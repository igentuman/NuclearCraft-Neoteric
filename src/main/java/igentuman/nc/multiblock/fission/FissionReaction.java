package igentuman.nc.multiblock.fission;

import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.energy.CustomEnergyStorage;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.recipe.fission.BoilingRecipe;
import igentuman.nc.recipe.fission.FissionFuelRecipe;
import igentuman.nc.recipe.fission.FissionRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.BoilingBuffer;
import igentuman.nc.util.HeatBuffer;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.binding.RadiationBindings;
import igentuman.nr.corium.Corium;
import igentuman.nr.particle.MeltdownParticles;
import igentuman.nr.util.tracking.LeftOverRadSource;
import igentuman.nr.util.tracking.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import static igentuman.nc.block.MultiblockControllerBlock.FACING;

/** Runs the fission reactor's per-tick reaction: heat, energy/steam generation, cooling, fuel burn, and meltdown. */
public class FissionReaction {

    private static final double GENERATION_MULTIPLIER = 1;
    private static final int IRRADIATION_HEAT_PER_LINE = 15;

    private double reactivityLevel;
    private double ticksProcessed;
    public FissionFuelRecipe currentRecipe;
    private ItemStack lastFuel = ItemStack.EMPTY;
    private BoilingRecipe currentBoiling;
    private Fluid lastBoilFluid;
    private boolean fuelConsumed = false;
    private ItemStack consumedFuelStack = ItemStack.EMPTY;

    public void tick(FissionReactorControllerBE be, FissionReactorCache fc) {
        HeatBuffer heat = be.heatBuffer();
        Level level = be.getLevel();

        double volume = Math.max(1, (double) fc.width * fc.height * fc.depth);
        double sizeMult = Math.max(1, Math.round(Math.log(volume) * 10) / 10.0 - 1);
        heat.setCapacity(Multiblocks.fissionHeatCapacity * sizeMult);

        IItemHandler items = be.getItemHandler(null);
        FissionFuelRecipe recipe;
        if (fuelConsumed && currentRecipe != null) {
            recipe = currentRecipe;
        } else {
            ItemStack fuel = items != null ? items.getStackInSlot(0) : ItemStack.EMPTY;
            recipe = findRecipe(level, fuel);
        }
        int fuelCells = fc.fuelCellCount;
        boolean processing = recipe != null && fuelCells > 0;

        double activeCooling = applyActiveCooling(be, fc, heat.currentHeat > 0 || processing);
        double cooling = fc.totalCooling + activeCooling;

        if (!processing) {
            if (fuelConsumed) {
                returnFuel(items);
            }
            reactivityLevel = Math.max(0, reactivityLevel - 1);
            heat.heatPerTick = 0;
            heat.cooldownPerTick = cooling;
            heat.cool();
            be.energyPerTick = 0;
            be.steamPerTick = 0;
            be.boilingBuffer().reset();
            syncDisplay(be, heat, recipe);
            return;
        }

        if (!fuelConsumed && items != null) {
            consumedFuelStack = items.extractItem(0, 1, false);
            fuelConsumed = !consumedFuelStack.isEmpty();
        }

        reactivityLevel = clamp(reactivityLevel + 1, 0, 100);

        double mod = be.moderationFactor();
        double cellsHeat = fc.cellsHeatMult + fc.moderatorsHeatMult;
        double cellsEnergy = fc.cellsEnergyMult + fc.moderatorsEnergyMult;
        double heatPerTick = (recipe.heat() * cellsHeat * Multiblocks.fissionFuelHeatMultiplier
                + fc.irradiationLines * (double) IRRADIATION_HEAT_PER_LINE) * mod;
        double heatMult = heatMultiplier(heatPerTick, cooling);
        double factor = heatMult + collectedHeatMultiplier(heat.currentHeat, heat.capacity) - 1;

        ticksProcessed += Math.max(0, fuelCells * factor * reactivityLevel / 100.0);

        if (be.isSteamMode()) {
            be.energyPerTick = 0;
            be.steamPerTick = boil(be, level, cooling, heatMult, mod);
        } else {
            int gen = (int) (recipe.power() * cellsEnergy * factor
                    * Multiblocks.fissionFeGenerationMultiplier / 10.0 * GENERATION_MULTIPLIER * reactivityLevel / 100.0 * mod);
            addEnergy(be.energyStorage, gen);
            be.energyPerTick = Math.max(0, gen);
            be.steamPerTick = 0;
            be.boilingBuffer().reset();
        }

        heat.heatPerTick = heatPerTick;
        heat.addHeat(heatPerTick * Math.max(0.5, reactivityLevel / 100.0));
        heat.cooldownPerTick = cooling;
        heat.cool();

        if (recipe.processTime() > 0 && ticksProcessed >= effectiveProcessTime(recipe)) {
            if (produceOutput(items, recipe)) {
                ticksProcessed = 0;
                fuelConsumed = false;
                consumedFuelStack = ItemStack.EMPTY;
                currentRecipe = null;
            }
        }

        be.markDirty();
        if (heat.isOverMax()) {
            meltdown(be, fc);
            return;
        }
        syncDisplay(be, heat, recipe);
    }

    /** Called when the structure is not formed: bleed off reactivity and clear display values. */
    public void idle(FissionReactorControllerBE be) {
        reactivityLevel = Math.max(0, reactivityLevel - 1);
        if (fuelConsumed) {
            returnFuel(be.getItemHandler(null));
        }
        HeatBuffer heat = be.heatBuffer();
        heat.heatPerTick = 0;
        heat.cooldownPerTick = 0;
        be.energyPerTick = 0;
        be.steamPerTick = 0;
        be.boilingBuffer().reset();
        syncDisplay(be, heat, null);
    }

    private void returnFuel(IItemHandler items) {
        if (!consumedFuelStack.isEmpty() && items != null) {
            items.insertItem(0, consumedFuelStack.copy(), false);
        }
        consumedFuelStack = ItemStack.EMPTY;
        fuelConsumed = false;
        ticksProcessed = 0;
        currentRecipe = null;
        lastFuel = ItemStack.EMPTY;
    }

    /** Drains each supplied active-coolant tank and returns the cooling those sinks contribute.
     *  A coolant type only cools when its tank holds at least the full per-tick demand. */
    private double applyActiveCooling(FissionReactorControllerBE be, FissionReactorCache fc, boolean consume) {
        int[] counts = fc.activeCoolantCounts;
        if (counts == null) return 0;
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return 0;
        double cooling = 0;
        for (ActiveCoolant c : ActiveCoolant.VALUES) {
            int n = c.ordinal() < counts.length ? counts[c.ordinal()] : 0;
            if (n <= 0) continue;
            int required = n * Multiblocks.fissionActiveCoolantPerTick;
            FluidStack inTank = tanks.getFluidInTank(c.tankIndex());
            if (inTank.getAmount() < required) continue;
            if (consume) tanks.drainTank(c.tankIndex(), required, IFluidHandler.FluidAction.EXECUTE);
            cooling += n * c.sinkHeat;
        }
        return cooling;
    }

    /** Converts available cooling throughput into steam, consuming boiling coolant from the input
     *  tank and filling the steam output tank. Returns mB of steam produced this tick. */
    private int boil(FissionReactorControllerBE be, Level level, double cooling, double heatMult, double mod) {
        BoilingBuffer buf = be.boilingBuffer();
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) {
            buf.reset();
            return 0;
        }
        int inTank = ActiveCoolant.BOILING_INPUT_TANK;
        int outTank = ActiveCoolant.STEAM_OUTPUT_TANK;
        buf.capacity = tanks.getTankCapacity(inTank);
        buf.boilingRate = 0;
        buf.maxBoilingRate = 0;

        FluidStack coolant = tanks.getFluidInTank(inTank);
        buf.coolantAmount = coolant.getAmount();
        buf.hotCoolantAmount = tanks.getFluidInTank(outTank).getAmount();
        if (coolant.isEmpty()) return 0;

        BoilingRecipe recipe = findBoilingRecipe(level, coolant);
        if (recipe == null || recipe.heatRequired() <= 0) return 0;

        double heatEff = cooling * (Multiblocks.fissionBoilingMult / 100.0) * heatMult * mod;
        int maxOps = (int) (heatEff / recipe.heatRequired());
        if (maxOps <= 0) return 0;

        int inAmount = recipe.input().amount();
        FluidStack out = recipe.output().resolve();
        if (inAmount <= 0 || out.isEmpty()) return 0;
        buf.maxBoilingRate = (double) maxOps * out.getAmount();

        int ops = Math.min(maxOps, coolant.getAmount() / inAmount);
        FluidStack curOut = tanks.getFluidInTank(outTank);
        if (!curOut.isEmpty() && curOut.getFluid() != out.getFluid()) return 0;
        int space = tanks.getTankCapacity(outTank) - curOut.getAmount();
        ops = Math.min(ops, space / out.getAmount());
        if (ops <= 0) return 0;

        tanks.drainTank(inTank, ops * inAmount, IFluidHandler.FluidAction.EXECUTE);
        tanks.fillTank(outTank, new FluidStack(out.getFluid(), ops * out.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        int produced = ops * out.getAmount();
        buf.boilingRate = produced;
        buf.coolantAmount = tanks.getFluidInTank(inTank).getAmount();
        buf.hotCoolantAmount = tanks.getFluidInTank(outTank).getAmount();
        return produced;
    }

    private FissionFuelRecipe findRecipe(Level level, ItemStack fuel) {
        if (fuel.isEmpty()) {
            currentRecipe = null;
            lastFuel = ItemStack.EMPTY;
            return null;
        }
        if (currentRecipe != null && ItemStack.isSameItem(fuel, lastFuel)) {
            return currentRecipe;
        }
        if (!(level instanceof ServerLevel sl)) {
            return currentRecipe;
        }
        for (RecipeHolder<FissionFuelRecipe> holder : sl.getRecipeManager().getAllRecipesFor(FissionRecipes.FUEL_TYPE.get())) {
            if (holder.value().input().test(fuel)) {
                currentRecipe = holder.value();
                lastFuel = fuel.copy();
                return currentRecipe;
            }
        }
        currentRecipe = null;
        lastFuel = ItemStack.EMPTY;
        return null;
    }

    private BoilingRecipe findBoilingRecipe(Level level, FluidStack coolant) {
        if (currentBoiling != null && coolant.getFluid() == lastBoilFluid) {
            return currentBoiling;
        }
        if (!(level instanceof ServerLevel sl)) {
            return currentBoiling;
        }
        for (RecipeHolder<BoilingRecipe> holder : sl.getRecipeManager().getAllRecipesFor(FissionRecipes.BOILING_TYPE.get())) {
            if (holder.value().input().ingredient().test(coolant)) {
                currentBoiling = holder.value();
                lastBoilFluid = coolant.getFluid();
                return currentBoiling;
            }
        }
        currentBoiling = null;
        lastBoilFluid = null;
        return null;
    }

    private boolean produceOutput(IItemHandler items, FissionFuelRecipe recipe) {
        ItemStack out = recipe.output().resolve();
        if (out.isEmpty()) return true;
        ItemStack remainder = items.insertItem(1, out.copy(), true);
        if (!remainder.isEmpty()) return false;
        items.insertItem(1, out.copy(), false);
        return true;
    }

    private void addEnergy(CustomEnergyStorage es, int amount) {
        if (es == null || amount <= 0) return;
        int cur = es.getEnergyStored();
        int add = Math.min(amount, es.getMaxEnergyStored() - cur);
        if (add > 0) es.setEnergyStored(cur + add);
    }

    private void meltdown(FissionReactorControllerBE be, FissionReactorCache fc) {
        Level level = be.getLevel();
        ItemStack fuelStack = consumedFuelStack;
        if (fuelStack.isEmpty()) {
            IItemHandler items = be.getItemHandler(null);
            fuelStack = items != null ? items.getStackInSlot(0) : ItemStack.EMPTY;
        }
        int fuelCells = fc.fuelCellCount;
        if (level instanceof ServerLevel sl) {
            BlockPos p = be.getBlockPos();
            BlockPos center = Objects.requireNonNull(MultiblockHandler.getInstance(sl, p)).getCenter(fc);

            double radius = Multiblocks.fissionExplosionRadius;
            if (radius > 0) {
                sl.explode(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                        (float) radius, Level.ExplosionInteraction.BLOCK);
            }
            spillCorium(sl, fc);
            int topY = breakCeilingBlocks(sl, fc, center);
            spawnMeltdownRadiation(sl, p, fuelStack, fuelCells);
            BlockPos emissionCenter = new BlockPos(center.getX(), topY, center.getZ());
            MeltdownParticles.emit(sl, emissionCenter);
        }
        be.heatBuffer().reset();
        reactivityLevel = 0;
        ticksProcessed = 0;
        fuelConsumed = false;
        consumedFuelStack = ItemStack.EMPTY;
        currentRecipe = null;
        lastFuel = ItemStack.EMPTY;
    }

    /** Replaces every fuel-cell block with a molten corium source block. Best-effort: the cell
     *  position set is owned by the validator thread, so a concurrent rebuild just spills fewer. */
    private void spillCorium(ServerLevel sl, FissionReactorCache fc) {
        BlockState coriumState = Corium.MOLTEN_CORIUM.get().defaultFluidState().createLegacyBlock();
        long[] cells;
        try {
            Long[] snapshot = fc.fuelCells.toArray(new Long[0]);
            cells = new long[snapshot.length];
            for (int i = 0; i < snapshot.length; i++) cells[i] = snapshot[i];
        } catch (Exception e) {
            return;
        }
        for (long key : cells) {
            sl.setBlock(BlockPos.of(key), coriumState, Block.UPDATE_ALL);
        }
    }

    private int breakCeilingBlocks(ServerLevel sl, FissionReactorCache fc, BlockPos center) {
        int maxY = Integer.MIN_VALUE;
        for (long key : fc.getStructurePositions()) {
            int y = BlockPos.of(key).getY();
            if (y > maxY) maxY = y;
        }
        if (maxY == Integer.MIN_VALUE) return 0;

        List<BlockPos> ceiling = new ArrayList<>();
        for (long key : fc.getStructurePositions()) {
            BlockPos p = BlockPos.of(key);
            if (p.getY() == maxY && !sl.getBlockState(p).isAir()) {
                ceiling.add(p);
            }
        }
        if (!ceiling.isEmpty()) {
            int cx = center.getX();
            int cz = center.getZ();
            ceiling.sort((a, b) -> {
                double da = (a.getX() - cx) * (a.getX() - cx) + (a.getZ() - cz) * (a.getZ() - cz);
                double db = (b.getX() - cx) * (b.getX() - cx) + (b.getZ() - cz) * (b.getZ() - cz);
                return Double.compare(da, db);
            });
            int count = Math.min(ceiling.size(), 2 + sl.getRandom().nextInt(4));
            for (int i = 0; i < count; i++) {
                sl.destroyBlock(ceiling.get(i), true);
            }
        }
        return maxY;
    }

    /** Registers a persistent leftover radiation source whose activity scales with the fuel-cell
     *  count and the isotope profile bound to the loaded fuel. */
    private void spawnMeltdownRadiation(ServerLevel sl, BlockPos pos, ItemStack fuelStack, int fuelCells) {
        if (fuelCells <= 0) return;
        RadiationProfile fuelProfile = RadiationBindings.of(fuelStack);
        if (fuelProfile.isEmpty()) return;
        long now = sl.getGameTime();
        RadiationProfile leftover = RadiationProfile.empty();
        leftover.mergeAtoms(fuelProfile, fuelCells, now);
        WorldSourceRegistry registry = WorldSourceRegistry.get(sl);
        registry.register(new LeftOverRadSource(sl, pos, leftover, now, true));
        RadiationProfile chunk = leftover.copy(now);
        chunk.reduceAtoms(2);
        registry.setChunkRadiation(pos, RadiationProfile.empty(), RadiationProfile.empty(), chunk);
    }

    private double heatMultiplier(double h, double cooling) {
        if (h <= 0) return 1;
        double c = Math.max(1, cooling);
        double v = Math.log10(h / c) / (1 + Math.exp(h / c * Multiblocks.fissionHeatMultiplier)) + 1;
        return Math.round(v * 100) / 100.0;
    }

    private double collectedHeatMultiplier(double heat, double maxHeat) {
        if (maxHeat <= 0) return 1;
        return Math.min(Multiblocks.fissionHeatMultiplierCap, Math.pow((heat + maxHeat / 8) / maxHeat, 5) + 0.9999694824);
    }

    private void syncDisplay(FissionReactorControllerBE be, HeatBuffer heat, FissionFuelRecipe recipe) {
        be.reactivity = (int) reactivityLevel;
        be.progress = (recipe != null && recipe.processTime() > 0)
                ? (int) Math.min(100, ticksProcessed / effectiveProcessTime(recipe) * 100) : 0;
        be.maxProgress = 100;
    }

    /** Fuel burn duration after applying the depletion multiplier (higher = fuel lasts longer). */
    private static double effectiveProcessTime(FissionFuelRecipe recipe) {
        return recipe.processTime() * Multiblocks.fissionDepletionMultiplier;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putDouble("reactivity", reactivityLevel);
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putBoolean("fuelConsumed", fuelConsumed);
        if (fuelConsumed && !consumedFuelStack.isEmpty()) {
            tag.put("consumedFuel", consumedFuelStack.save(registries));
        }
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        reactivityLevel = tag.getDouble("reactivity");
        ticksProcessed = tag.getDouble("ticksProcessed");
        fuelConsumed = tag.getBoolean("fuelConsumed");
        if (fuelConsumed && tag.contains("consumedFuel")) {
            consumedFuelStack = ItemStack.parseOptional(registries, tag.getCompound("consumedFuel"));
        } else {
            consumedFuelStack = ItemStack.EMPTY;
        }
    }

    public double reactivity() {
        return reactivityLevel;
    }
}
