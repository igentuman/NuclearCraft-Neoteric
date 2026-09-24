package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.block_entity.turbine.TurbineControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.recipe.turbine.TurbineRecipe;
import igentuman.nc.recipe.turbine.TurbineRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class TurbineLogic extends AbstractMultiblockLogic<TurbineCache> {

    private float rotationSpeed;

    @Override
    public void tickServer(ServerLevel level, BlockPos controllerPos, TurbineCache tc) {
        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        if (!(blockEntity instanceof TurbineControllerBE be)) return;
        double realFlow = 0;
        double maxFlow = 0;
        int genPerTick = 0;
        int maxGen = 0;
        double powerMod = 1.0;

        FluidStackHandler tanks = be.fluidTanks();
        if (tc.flow > 0 && tc.bladeCount > 0) {
            double flowD = Math.max(1, tc.flow);
            maxFlow = flowD * Multiblocks.turbineBladeFlow * Math.pow(Math.log10(flowD), 2.8);
            double coilsDrag = tc.coilsEfficiency > 0
                    ? Math.max(1, 100.0 / tc.coilsEfficiency * Math.log(Math.log10(tc.activeCoils + 4) + 2))
                    : 1;

            FluidStack in = tanks != null ? tanks.getFluidInTank(0) : FluidStack.EMPTY;
            TurbineRecipe recipe = in.isEmpty() ? null : findRecipe(level, in);
            if (recipe != null) {
                powerMod = recipe.powerModifier();
                int requestedInput = (int) (Math.min(maxFlow, in.getAmount()) / coilsDrag);
                FluidStack output = recipe.output().resolve();
                int consumedPerOp = recipe.input().amount();
                int producedPerOp = output.getAmount();
                int inputForOutputRoom = consumedPerOp > 0 && producedPerOp > 0
                        ? (int) Math.min(Integer.MAX_VALUE, (long) availableOutput(tanks, output) * consumedPerOp / producedPerOp)
                        : 0;
                int inputAmount = Math.min(requestedInput, inputForOutputRoom);
                int outputAmount = consumedPerOp > 0 ? (int) ((long) inputAmount * producedPerOp / consumedPerOp) : 0;
                if (inputAmount > 0 && outputAmount > 0) {
                    realFlow = inputAmount;
                    genPerTick = generateEnergy(be.energyStorage, tc, realFlow, powerMod);
                    tanks.drainTank(0, inputAmount, EXECUTE);
                    tanks.fillTank(1, output.copyWithAmount(outputAmount), EXECUTE);
                }
            }
            maxGen = (int) computeEnergy(tc, maxFlow, powerMod);
        }

        float newSpeed = (float) ((rotationSpeed * 4 + realFlow / (Math.max(1, tc.flow) * Multiblocks.turbineBladeFlow)) / 5f);
        rotationSpeed = newSpeed < 0.001f ? 0f : newSpeed;
        be.updateRuntimeDisplay(tc, rotationSpeed, (int) Math.min(realFlow, maxFlow), (int) maxFlow, genPerTick, maxGen);
    }

    private static double computeEnergy(TurbineCache tc, double flowValue, double powerModifier) {
        if (tc.activeCoils < 1 || tc.coilsEfficiency <= 0 || tc.bladeCount <= 0) return 0;
        double bladesEfficiency = tc.flow / tc.bladeCount;
        double efficiencyRate = Math.log10(tc.activeCoils) * tc.coilsEfficiency * bladesEfficiency / 1000.0;
        if (efficiencyRate <= 0) return 0;
        return Math.sqrt((flowValue + 1) * (flowValue + 2) / 2.0)
                * Multiblocks.turbineEnergyGen * efficiencyRate * powerModifier * 4;
    }

    private static int generateEnergy(LargeEnergyStorage storage, TurbineCache tc, double realFlow, double powerModifier) {
        double energy = computeEnergy(tc, realFlow, powerModifier);
        if (energy <= 0 || storage == null) return 0;
        int add = (int) Math.min(energy, storage.getMaxEnergyStored() - storage.getEnergyStored());
        if (add > 0) storage.setEnergyStored(storage.getEnergyStored() + add);
        return (int) energy;
    }

    private static TurbineRecipe findRecipe(ServerLevel level, FluidStack in) {
        for (RecipeHolder<TurbineRecipe> holder : level.getRecipeManager().getAllRecipesFor(TurbineRecipes.TURBINE_TYPE.get())) {
            if (holder.value().input().test(in)) return holder.value();
        }
        return null;
    }

    private static int availableOutput(FluidStackHandler tanks, FluidStack output) {
        if (output.isEmpty() || output.getAmount() <= 0) return 0;
        FluidStack current = tanks.getFluidInTank(1);
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, output)) return 0;
        return Math.max(0, tanks.getTankCapacity(1) - current.getAmount());
    }

    @Override
    public void resetRuntime() {
        rotationSpeed = 0f;
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putFloat("rotationSpeed", rotationSpeed);
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        rotationSpeed = Math.max(0f, tag.getFloat("rotationSpeed"));
    }
}
