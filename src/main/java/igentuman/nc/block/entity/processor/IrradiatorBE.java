package igentuman.nc.block.entity.processor;

import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.IMultiblockAttachable;
import igentuman.nc.multiblock.INCMultiblockController;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

public class IrradiatorBE extends NCProcessorBE<IrradiatorBE.Recipe> implements IMultiblockAttachable {
    private AbstractNCMultiblock multiblock;
    private FissionControllerBE<?> controller;

    public IrradiatorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.IRRADIATOR);
    }
    @Override
    public String getName() {
        return Processors.IRRADIATOR;
    }

    @NBTField
    public int irradiativeFlux = 0;

    @NBTField
    public double fuelMultiplier = 1D;

    @Override
    public double speedMultiplier()
    {
        return (double)irradiativeFlux/10D*fuelMultiplier;
    }

    @Override
    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public FissionControllerBE<?> controller() {
        return controller;
    }

    @Override
    public AbstractNCMultiblock multiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    public void tickServer() {
        int wasFlux = irradiativeFlux;
        double wasFuel = fuelMultiplier;
        irradiativeFlux = 0;
        fuelMultiplier = 0;
        upadteMultiblockConnection();
        if (multiblock != null) {
            if (multiblock.isFormed()) {
                if (multiblock.controller() != null) {
                    controller = (FissionControllerBE<?>) multiblock.controller().controllerBE();
                    if(controller.isProcessing()) {
                        irradiativeFlux = controller.irradiationConnections;
                        fuelMultiplier = controller.recipeInfo.recipe().getRadiation()*10000;
                    }
                }
            }
        }
        if(speedMultiplier() > 0) {
            super.tickServer();
        }
        if(wasFlux != irradiativeFlux || wasFuel != fuelMultiplier) {
            setChanged();
        }
    }
    @Override
    protected void processRecipe() {
        if(!hasRecipe()) {
            updateRecipe();
        }
        if(!hasRecipe()) {
            isActive = false;
            return;
        }

        if(energyStorage.getEnergyStored() < energyPerTick()*skippedTicks) {
            isActive = false;
            return;
        }
        boolean processed = recipeInfo.process(speedMultiplier()*skippedTicks);
        if(processed) {
            controller().addIrradiationHeat();
        }
        if(recipeInfo.radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (recipeInfo.radiation/1000000)*speedMultiplier()*skippedTicks, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        isActive = true;
        setChanged();
        if(!recipeInfo.isCompleted() && hasRecipe()) {
            energyStorage.consumeEnergy(energyPerTick()*skippedTicks);
        }
    }

    public void upadteMultiblockConnection()
    {
        for (Direction d: Direction.values()) {
            if(d.equals(getFacing()) || d.equals(getFacing().getOpposite())) continue;
            BlockPos toCheck = getBlockPos().relative(d);
            BlockEntity be = getLevel().getBlockEntity(toCheck);
            if(be instanceof FissionBE) {
                multiblock = ((FissionBE) be).multiblock();
                controller = (FissionControllerBE<?>) ((FissionBE) be).controller();
            }
        }
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStack[] output,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output,inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.IRRADIATOR;
        }
    }
}
