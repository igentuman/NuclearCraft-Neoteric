package igentuman.nc.block.entity.processor;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class IrradiatorBE extends NCProcessorBE implements MultiblockAttachable {

    private AbstractNCMultiblock multiblock;
    private FissionControllerBE controller;
    @NBTField
    public int irradiativeFlux = 0;
    @NBTField
    public double fuelMultiplier = 1D;

    public IrradiatorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.IRRADIATOR);
        particle1 = ParticleTypes.HAPPY_VILLAGER;
    }

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
    public FissionControllerBE controller() {
        return controller;
    }

    @Override
    public AbstractNCMultiblock getMultiblock() {
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
        //upadteMultiblockConnection();
        if (controller() != null && controller().isProcessing()) {
            irradiativeFlux = controller().irradiationConnections;
            fuelMultiplier = controller().recipeInfo.recipe().getRadiation()*10000;
        }
        if(speedMultiplier() > 0) {
            super.tickServer();
        }
        if(wasFlux != irradiativeFlux || wasFuel != fuelMultiplier) {
            setChanged();
        }
    }

    @Override
    public void processRecipe() {
        if(!hasRecipe()) {
            updateRecipe();
        }
        if(!hasRecipe()) {
            isActive = false;
            return;
        }

        if(energyStorage().getEnergyStored() < energyPerTick()*skippedTicks) {
            isActive = false;
            return;
        }
        boolean processed = recipeInfo().process(speedMultiplier()*skippedTicks);
        if(processed) {
            controller().addIrradiationHeat();
        }
        if(recipeInfo().radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (recipeInfo().radiation/1000000)*speedMultiplier()*skippedTicks, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        isActive = true;
        setChanged();
        if(!recipeInfo().isCompleted() && hasRecipe()) {
            energyStorage().consumeEnergy(energyPerTick()*skippedTicks);
        }
    }

    @Deprecated
    public void upadteMultiblockConnection()
    {
        AbstractNCMultiblock mb = MultiblockHandler.getMultiblockByPos(getBlockPos());
        if(mb != null) {
            if(mb.isFormed()) {
                if(mb.controller() != null) {
                    controller = (FissionControllerBE) mb.controller().controllerBE();
                }
            }
            multiblock = mb;
        }
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output,inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.IRRADIATOR;
        }
    }
}
