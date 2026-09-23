package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.block_entity.heat_exchanger.HeatExchangerControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipe;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipes;
import igentuman.nc.util.HeatBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class HeatExchangerLogic extends AbstractMultiblockLogic<HeatExchangerCache> {

    private final HeatBuffer heat = new HeatBuffer();
    private int lastExchangers = -1;
    private int hotCycleOps;
    private int coldCycleOps;
    private boolean runtimeLoaded;

    @Override
    public void onBroken(ServerLevel level, BlockPos controllerPos, HeatExchangerCache cache) {
        super.onBroken(level, controllerPos, cache);
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof HeatExchangerControllerBE controller) controller.clearStats();
    }

    @Override
    public void resetRuntime() {
        lastExchangers = -1;
        hotCycleOps = 0;
        coldCycleOps = 0;
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putDouble("currentHeat", heat.currentHeat);
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        heat.currentHeat = Math.max(0, tag.getDouble("currentHeat"));
        runtimeLoaded = true;
    }

    @Override
    public void tickServer(ServerLevel level, BlockPos controllerPos, HeatExchangerCache cache) {
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (!(be instanceof HeatExchangerControllerBE controller)) return;
        if (!runtimeLoaded) {
            heat.currentHeat = Math.max(0, controller.heatBuffer.currentHeat);
            runtimeLoaded = true;
        }
        applyStructure(controller, cache);
        heat.heatPerTick = 0;
        heat.cooldownPerTick = 0;
        coolDown(controller);
        boolean powered = controller.heatExchangers > 0
                && level.hasNeighborSignal(controllerPos)
                && controller.energyStorage != null
                && controller.energyStorage.getEnergyStored() >= controller.energyPerTick();
        if (powered) {
            processLoops(level, controller);
        } else {
            hotCycleOps = 0;
            coldCycleOps = 0;
        }
        controller.updateRuntimeDisplay(heat, hotCycleOps, coldCycleOps);
    }

    private void applyStructure(HeatExchangerControllerBE controller, HeatExchangerCache cache) {
        if (controller.heatExchangers != cache.heatExchangers || controller.radiators != cache.radiators) {
            controller.heatExchangers = cache.heatExchangers;
            controller.radiators = cache.radiators;
            controller.markDirty();
        }
        controller.ensureValidators();
        if (controller.heatExchangers == lastExchangers) return;
        lastExchangers = controller.heatExchangers;
        int perBlock = Multiblocks.hxFluidCapacityPerBlock;
        int capacity = Math.max(perBlock, (controller.heatExchangers + 1) * perBlock);
        FluidStackHandler tanks = controller.fluidTanks();
        if (tanks != null) {
            for (int tank = 0; tank < 4; tank++) tanks.setTankCapacity(tank, capacity);
        }
        heat.setCapacity((double) controller.heatExchangers * Multiblocks.hxHeatCapacityPerBlock);
        if (heat.currentHeat > heat.capacity) heat.currentHeat = heat.capacity;
    }

    private void coolDown(HeatExchangerControllerBE controller) {
        if (!controller.radiatorsEnabled || controller.radiators <= 0) return;
        double removed = (double) controller.radiators * Multiblocks.hxRadiatorCooling;
        heat.cooldownPerTick += removed;
        heat.currentHeat = Math.max(0, heat.currentHeat - removed);
    }

    private void processLoops(ServerLevel level, HeatExchangerControllerBE controller) {
        hotCycleOps = 0;
        coldCycleOps = 0;
        boolean hotRan = processSide(level, controller, HeatExchangerControllerBE.TANK_HOT_IN,
                HeatExchangerControllerBE.TANK_HOT_OUT, true);
        boolean coldRan = processSide(level, controller, HeatExchangerControllerBE.TANK_COLD_IN,
                HeatExchangerControllerBE.TANK_COLD_OUT, false);
        if ((hotRan || coldRan) && controller.energyStorage != null) {
            controller.energyStorage.drainEnergy(controller.energyPerTick());
            controller.markDirty();
        }
    }

    private boolean processSide(ServerLevel level, HeatExchangerControllerBE controller,
                                int inTank, int outTank, boolean hot) {
        FluidStackHandler tanks = controller.fluidTanks();
        if (tanks == null) return false;
        FluidStack input = tanks.getFluidInTank(inTank);
        if (input.isEmpty()) return false;
        HeatExchangerRecipe recipe = findRecipe(level, input, hot);
        if (recipe == null) return false;

        int inputAmount = recipe.input().amount();
        FluidStack outputTemplate = recipe.output().resolve();
        if (inputAmount <= 0 || outputTemplate.isEmpty()) return false;
        int outputAmount = outputTemplate.getAmount();
        int recipeHeat = recipe.heat();

        double throughput = controller.heatExchangers * Multiblocks.hxThroughputPerBlock;
        long operations = (long) Math.floor(throughput / inputAmount);
        operations = Math.min(operations, input.getAmount() / (long) inputAmount);
        if (outputAmount > 0) {
            int room = tanks.getTankCapacity(outTank) - tanks.getFluidInTank(outTank).getAmount();
            operations = Math.min(operations, room / (long) outputAmount);
        }
        if (recipeHeat > 0) {
            operations = Math.min(operations, (long) ((heat.capacity - heat.currentHeat) / recipeHeat));
        } else if (recipeHeat < 0) {
            operations = Math.min(operations, (long) (heat.currentHeat / -recipeHeat));
        }
        if (operations <= 0) return false;

        int fillAmount = (int) (operations * outputAmount);
        FluidStack toOutput = new FluidStack(outputTemplate.getFluid(), fillAmount);
        if (tanks.fillTank(outTank, toOutput, SIMULATE) < fillAmount) return false;

        tanks.drainTank(inTank, (int) (operations * inputAmount), EXECUTE);
        tanks.fillTank(outTank, toOutput, EXECUTE);
        if (recipeHeat > 0) {
            heat.currentHeat = Math.min(heat.capacity, heat.currentHeat + (double) operations * recipeHeat);
            heat.heatPerTick += (double) operations * recipeHeat;
        } else {
            heat.currentHeat = Math.max(0, heat.currentHeat + (double) operations * recipeHeat);
            heat.cooldownPerTick += (double) operations * -recipeHeat;
        }
        if (hot) hotCycleOps = (int) operations;
        else coldCycleOps = (int) operations;
        controller.markDirty();
        return true;
    }

    private HeatExchangerRecipe findRecipe(ServerLevel level, FluidStack input, boolean hot) {
        for (RecipeHolder<HeatExchangerRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(HeatExchangerRecipes.HX_TYPE.get())) {
            HeatExchangerRecipe recipe = holder.value();
            if (hot ? !recipe.isHot() : !recipe.isCold()) continue;
            if (recipe.input().test(input)) return recipe;
        }
        return null;
    }
}
