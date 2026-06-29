package igentuman.nc.multiblock.fission;

import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.handler.energy.CustomEnergyStorage;
import igentuman.nc.recipe.fission.FissionFuelRecipe;
import igentuman.nc.recipe.fission.FissionRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Fission reactor runtime reaction (energy mode). Runs on the main server thread from the
 * controller BE tick; reads structure stats from {@link FissionReactorCache} (scalars only) and
 * mutates the controller's heat buffer, energy storage, and fuel inventory. Steam mode is added
 * in a later phase.
 *
 * <p>Formulas mirror NuclearCraft Neoteric; radiation is out of scope.
 */
public class FissionReaction {

    private static final double HEAT_CAPACITY = 1_000_000;
    private static final double HEAT_MULTIPLIER = 1;
    private static final double HEAT_MULTIPLIER_CAP = 3;
    private static final double FE_GENERATION_MULTIPLIER = 10;
    private static final double GENERATION_MULTIPLIER = 1;
    private static final int EXPLOSION_RADIUS = 4;
    private static final int IRRADIATION_HEAT_PER_LINE = 15;

    private double reactivityLevel;
    private double ticksProcessed;
    private FissionFuelRecipe currentRecipe;
    private ItemStack lastFuel = ItemStack.EMPTY;

    public void tick(FissionReactorControllerBE be, FissionReactorCache fc) {
        HeatBuffer heat = be.heatBuffer();
        Level level = be.getLevel();

        double volume = Math.max(1, (double) fc.width * fc.height * fc.depth);
        double sizeMult = Math.max(1, Math.round(Math.log(volume) * 10) / 10.0 - 1);
        heat.setCapacity(HEAT_CAPACITY * sizeMult);

        IItemHandler items = be.getItemHandler(null);
        ItemStack fuel = items != null ? items.getStackInSlot(0) : ItemStack.EMPTY;
        FissionFuelRecipe recipe = findRecipe(level, fuel);
        int fuelCells = fc.fuelCellCount;

        boolean enabled = true; // redstone control deferred to a later phase

        if (recipe == null || fuelCells <= 0) {
            reactivityLevel = Math.max(0, reactivityLevel - 1);
            heat.heatPerTick = 0;
            heat.cooldownPerTick = fc.totalCooling;
            heat.cool();
            be.energyPerTick = 0;
            syncDisplay(be, heat, recipe);
            return;
        }

        reactivityLevel = clamp(reactivityLevel + (enabled ? 1 : -1), 0, 100);

        double cellsHeat = fc.cellsHeatMult + fc.moderatorsHeatMult;
        double cellsEnergy = fc.cellsEnergyMult + fc.moderatorsEnergyMult;
        double cooling = fc.totalCooling;
        double heatPerTick = recipe.heat() * cellsHeat + fc.irradiationLines * (double) IRRADIATION_HEAT_PER_LINE;
        double factor = heatMultiplier(heatPerTick, cooling) + collectedHeatMultiplier(heat.currentHeat, heat.capacity) - 1;

        ticksProcessed += Math.max(0, fuelCells * factor * reactivityLevel / 100.0);

        int gen = (int) (recipe.power() * cellsEnergy * factor
                * FE_GENERATION_MULTIPLIER / 10.0 * GENERATION_MULTIPLIER * reactivityLevel / 100.0);
        addEnergy(be.energyStorage, gen);
        be.energyPerTick = Math.max(0, gen);

        heat.heatPerTick = heatPerTick;
        heat.addHeat(heatPerTick * Math.max(0.5, reactivityLevel / 100.0));
        heat.cooldownPerTick = cooling;
        heat.cool();

        if (recipe.processTime() > 0 && ticksProcessed >= recipe.processTime()) {
            if (produceOutput(items, recipe)) {
                items.extractItem(0, 1, false);
                ticksProcessed = 0;
                currentRecipe = null;
            }
        }

        be.markDirty();
        if (heat.isOverMax()) {
            meltdown(be);
            return;
        }
        syncDisplay(be, heat, recipe);
    }

    /** Called when the structure is not formed: bleed off reactivity and clear display values. */
    public void idle(FissionReactorControllerBE be) {
        reactivityLevel = Math.max(0, reactivityLevel - 1);
        HeatBuffer heat = be.heatBuffer();
        heat.heatPerTick = 0;
        heat.cooldownPerTick = 0;
        be.energyPerTick = 0;
        syncDisplay(be, heat, null);
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

    private void meltdown(FissionReactorControllerBE be) {
        Level level = be.getLevel();
        if (level instanceof ServerLevel sl) {
            BlockPos p = be.getBlockPos();
            sl.explode(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                    EXPLOSION_RADIUS, Level.ExplosionInteraction.BLOCK);
        }
        be.heatBuffer().reset();
        reactivityLevel = 0;
        ticksProcessed = 0;
        currentRecipe = null;
    }

    private double heatMultiplier(double h, double cooling) {
        if (h <= 0) return 1;
        double c = Math.max(1, cooling);
        double v = Math.log10(h / c) / (1 + Math.exp(h / c * HEAT_MULTIPLIER)) + 1;
        return Math.round(v * 100) / 100.0;
    }

    private double collectedHeatMultiplier(double heat, double maxHeat) {
        if (maxHeat <= 0) return 1;
        return Math.min(HEAT_MULTIPLIER_CAP, Math.pow((heat + maxHeat / 8) / maxHeat, 5) + 0.9999694824);
    }

    private void syncDisplay(FissionReactorControllerBE be, HeatBuffer heat, FissionFuelRecipe recipe) {
        be.heat = (int) heat.currentHeat;
        be.maxHeat = (int) heat.capacity;
        be.reactivity = (int) reactivityLevel;
        be.cooling = (int) heat.cooldownPerTick;
        be.netHeat = (int) heat.netRate();
        be.progress = (recipe != null && recipe.processTime() > 0)
                ? (int) Math.min(100, ticksProcessed / recipe.processTime() * 100) : 0;
        be.maxProgress = 100;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public void save(CompoundTag tag) {
        tag.putDouble("reactivity", reactivityLevel);
        tag.putDouble("ticksProcessed", ticksProcessed);
    }

    public void load(CompoundTag tag) {
        reactivityLevel = tag.getDouble("reactivity");
        ticksProcessed = tag.getDouble("ticksProcessed");
    }

    public double reactivity() {
        return reactivityLevel;
    }
}
