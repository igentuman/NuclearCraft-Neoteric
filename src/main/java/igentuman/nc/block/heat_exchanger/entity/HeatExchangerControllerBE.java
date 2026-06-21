package igentuman.nc.block.heat_exchanger.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerMultiblock;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.HeatExchangerConfig.HEAT_EXCHANGER_CONFIG;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_BLOCKS;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class HeatExchangerControllerBE extends MultiblockControllerBE {

    public static String NAME = "heat_exchanger_controller";

    // Single content handler, 4 fluid tanks. FluidCapabilityHandler orders inputs-first, so:
    //   tank 0 = hot input,  tank 1 = cold input  (input region)
    //   tank 2 = hot output, tank 3 = cold output (output region)
    public static final int TANK_HOT_IN = 0;
    public static final int TANK_COLD_IN = 1;
    public static final int TANK_HOT_OUT = 2;
    public static final int TANK_COLD_OUT = 3;

    @NBTField
    public int heatExchangers = 0;
    @NBTField
    public int radiators = 0;
    @NBTField
    public double heat = 0;
    @NBTField
    public double maxHeat = 0;

    public final RecipeInfo coldRecipeInfo = new RecipeInfo();

    private List<FluidStack> hotInputs;
    private List<FluidStack> coldInputs;

    public HeatExchangerControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(HeatExchangerRegistration.HX_BE.get(NAME).get(), pPos, pBlockState);

        int cap = HEAT_EXCHANGER_CONFIG.FLUID_CAPACITY.get();
        contentHandler = new SidedContentHandler(
                0, 0,
                2, 2, cap, cap);
        contentHandler().fluidHandler.setGlobalMode(TANK_HOT_IN, SlotModePair.SlotMode.INPUT);
        contentHandler().fluidHandler.setGlobalMode(TANK_COLD_IN, SlotModePair.SlotMode.INPUT);
        contentHandler().fluidHandler.setGlobalMode(TANK_HOT_OUT, SlotModePair.SlotMode.OUTPUT);
        contentHandler().fluidHandler.setGlobalMode(TANK_COLD_OUT, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setBlockEntity(this);
        contentHandler().setAllowedInputFluids(TANK_HOT_IN, this::getHotInputs);
        contentHandler().setAllowedInputFluids(TANK_COLD_IN, this::getColdInputs);
        energyStorage = createEnergy();
        energy = net.minecraftforge.common.util.LazyOptional.of(() -> energyStorage);
        recipeInfo().be = this;
        coldRecipeInfo.be = this;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(HEAT_EXCHANGER_CONFIG.ENERGY_CAPACITY.get(), 1_000_000_000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public void voidFluidSlot(int slotId) {
        if (contentHandler() != null) {
            contentHandler().voidFluidSlot(slotId);
            setChanged();
        }
    }

    private List<FluidStack> collectInputs(boolean hot) {
        List<FluidStack> result = new ArrayList<>();
        for (NcRecipe recipe : NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
            if (!(recipe instanceof Recipe r)) continue;
            if (hot ? !r.isHot() : !r.isCold()) continue;
            for (FluidStackIngredient ingredient : r.getInputFluids()) {
                result.addAll(ingredient.getRepresentations());
            }
        }
        return result;
    }

    public List<FluidStack> getHotInputs() {
        if (hotInputs == null) {
            hotInputs = collectInputs(true);
        }
        return hotInputs;
    }

    public List<FluidStack> getColdInputs() {
        if (coldInputs == null) {
            coldInputs = collectInputs(false);
        }
        return coldInputs;
    }

    public boolean isAllowedInput(boolean hot, FluidStack stack) {
        for (FluidStack s : (hot ? getHotInputs() : getColdInputs())) {
            if (s.isFluidEqual(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public HeatExchangerMultiblock getMultiblock() {
        if (getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if (multiblock == null) {
            multiblock = new HeatExchangerMultiblock(this);
            validationsCounter = 0;
        }
        return (HeatExchangerMultiblock) multiblock;
    }


    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            return;
        }
        if (lastTickTime == currentTick) {
            return;
        }
        lastTickTime = currentTick;
        super.tickServer();
        changed = false;
        boolean wasPowered = powered;
        handleValidation();
        trackChanges(wasPowered, powered);
        controllerEnabled = getMultiblock().isFormed() && !forceShutdown && hasRedstoneSignal();

        if (getMultiblock().isFormed()) {
            trackChanges(contentHandler().tick());
            coolDown();
            if (controllerEnabled) {
                powered = processRecipes();
                trackChanges(powered);
            } else {
                powered = false;
            }
        }
        refreshCacheFlag = !getMultiblock().isFormed() || currentTick % 100 == 0;
        if (wasPowered != powered) {
            //MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered));
        }
        if (refreshCacheFlag || changed) {
            try {
                //MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(BlockStateProperties.POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {
            }
        }
    }


    // Passive cooling. Ungated: runs whenever the multiblock is formed, regardless of redstone/energy.
    private void coolDown() {
        if (radiators <= 0 || heat <= 0) {
            return;
        }
        double removed = (double) radiators * HEAT_EXCHANGER_CONFIG.RADIATOR_COOLING.get();
        double newHeat = Math.max(0, heat - removed);
        if (newHeat != heat) {
            heat = newHeat;
            setChanged();
        }
    }

    private boolean processRecipes() {
        if (heatExchangers <= 0) {
            return false;
        }
        int ept = energyPerTick();
        if (energyStorage().getEnergyStored() < ept) {
            return false;
        }
        boolean hotRan = processSide(recipeInfo(), TANK_HOT_IN, TANK_HOT_OUT, true);
        boolean coldRan = processSide(coldRecipeInfo, TANK_COLD_IN, TANK_COLD_OUT, false);
        if (hotRan || coldRan) {
            energyStorage().consumeEnergy(ept);
            setChanged();
            return true;
        }
        return false;
    }

    private boolean processSide(RecipeInfo info, int inTank, int outTank, boolean hot) {
        FluidTank in = getFluidTank(inTank);
        FluidTank out = getFluidTank(outTank);

        Recipe r = (Recipe) info.recipe();
        if (r == null || r.getInputFluids().length == 0 || !r.getInputFluids()[0].test(in.getFluid())) {
            r = findRecipe(in, hot);
            if (r == null) {
                info.clear();
                return false;
            }
            info.setRecipe(r);
            info.ticks = r.getBaseTime();
            info.ticksProcessed = 0;
            info.stuck = false;
        }

        if (r.getOutputFluids().isEmpty()) {
            return false;
        }
        int inAmount = r.getInputFluids()[0].getAmount();
        FluidStack outTemplate = r.getOutputFluids().get(0);
        int outAmount = outTemplate.getAmount();
        double recipeHeat = r.getHeat();
        if (inAmount <= 0) {
            return false;
        }

        // One "op" = one full recipe application. Cap ops/tick by every constraint, then run the minimum.
        double throughput = heatExchangers * HEAT_EXCHANGER_CONFIG.THROUGHPUT_PER_BLOCK.get();
        long ops = (long) Math.floor(throughput / inAmount);

        // Input on hand.
        ops = Math.min(ops, in.getFluidAmount() / (long) inAmount);

        // Free room in output tank.
        if (outAmount > 0) {
            int outRoom = out.getCapacity() - out.getFluidAmount();
            ops = Math.min(ops, outRoom / (long) outAmount);
        }

        // Heat buffer: hot dumps heat (limited by free space), cold pulls heat (limited by stored heat).
        if (recipeHeat > 0) {
            ops = Math.min(ops, (long) ((maxHeat - heat) / recipeHeat));
        } else if (recipeHeat < 0) {
            ops = Math.min(ops, (long) (heat / -recipeHeat));
        }

        if (ops <= 0) {
            info.stuck = in.getFluidAmount() >= inAmount; // input present but blocked
            return false;
        }

        int fillAmount = (int) (ops * outAmount);
        FluidStack toOutput = new FluidStack(outTemplate.getFluid(), fillAmount);
        if (out.fill(toOutput, SIMULATE) < fillAmount) {
            info.stuck = true;
            return false; // output fluid mismatch / no room
        }

        in.drain((int) (ops * inAmount), EXECUTE);
        out.fill(toOutput, EXECUTE);
        if (recipeHeat > 0) {
            heat = Math.min(maxHeat, heat + ops * recipeHeat);
        } else {
            heat = Math.max(0, heat + ops * recipeHeat);
        }

        info.stuck = false;
        // Running-flow indicator for the GUI arrow; processing itself is per-tick, not accumulated.
        info.ticksProcessed += 1;
        if (info.ticks <= 0 || info.ticksProcessed >= info.ticks) {
            info.ticksProcessed = 0;
        }
        return true;
    }

    private Recipe findRecipe(FluidTank in, boolean hot) {
        if (in.getFluid().isEmpty()) {
            return null;
        }
        for (NcRecipe rec : NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
            if (!(rec instanceof Recipe r)) continue;
            if (hot ? !r.isHot() : !r.isCold()) continue;
            if (r.getInputFluids().length == 0) continue;
            if (r.getInputFluids()[0].test(in.getFluid())) {
                return r;
            }
        }
        return null;
    }

    public int energyPerTick() {
        energyPerTick = heatExchangers * HEAT_EXCHANGER_CONFIG.ENERGY_PER_BLOCK.get();
        return energyPerTick;
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe() != null || coldRecipeInfo.recipe() != null;
    }

    public boolean isProcessing() {
        return (recipeInfo().recipe() != null && recipeInfo().ticksProcessed > 0 && !recipeInfo().isCompleted())
                || (coldRecipeInfo.recipe() != null && coldRecipeInfo.ticksProcessed > 0 && !coldRecipeInfo.isCompleted());
    }

    public double getProgress() {
        return recipeInfo().getProgress();
    }

    public double getColdProgress() {
        return coldRecipeInfo.getProgress();
    }

    public int getHeatExchangers() {
        return heatExchangers;
    }

    public int getRadiators() {
        return radiators;
    }

    public double getHeat() {
        return heat;
    }

    public double getMaxHeat() {
        return maxHeat;
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    @Override
    public void refresh() {
        needToUpdate = true;
        int perBlock = HEAT_EXCHANGER_CONFIG.FLUID_CAPACITY.get();
        int cap = Math.max(perBlock, (heatExchangers + 1) * perBlock);
        for (FluidTank tank : contentHandler().fluidHandler.tanks) {
            tank.setCapacity(cap);
        }
        maxHeat = (double) heatExchangers * HEAT_EXCHANGER_CONFIG.HEAT_CAPACITY_PER_BLOCK.get();
        if (heat > maxHeat) {
            heat = maxHeat;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("Info")) {
            CompoundTag info = tag.getCompound("Info");
            info.put("coldRecipeInfo", coldRecipeInfo.serializeNBT());
            tag.put("Info", info);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Info")) {
            CompoundTag info = tag.getCompound("Info");
            if (info.contains("coldRecipeInfo")) {
                coldRecipeInfo.deserializeNBT(info.getCompound("coldRecipeInfo"));
            }
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag info = tag.getCompound("Info");
            info.put("coldRecipeInfo", coldRecipeInfo.serializeNBT());
            tag.put("Info", info);
        }
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag info = tag.getCompound("Info");
            if (info.contains("coldRecipeInfo")) {
                coldRecipeInfo.deserializeNBT(info.getCompound("coldRecipeInfo"));
            }
        }
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(HeatExchangerControllerBE.NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return HeatExchangerControllerBE.NAME;
        }

        @Override
        public @NotNull String getGroup() {
            return HeatExchangerControllerBE.NAME;
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(HX_BLOCKS.get(getCodeId()).get());
        }

        public int getBaseTime() {
            return (int) Math.max(1, timeModifier);
        }

        public double getEnergy() {
            return Math.max(1, powerModifier);
        }

        public double getHeat() {
            return getRadiation();
        }

        public boolean isHot() {
            return getHeat() > 0;
        }

        public boolean isCold() {
            return getHeat() < 0;
        }
    }
}
