package igentuman.nc.block.entity.processor;

import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.block.ProcessorBlock.ACTIVE;

public class IrradiatorBE extends NCProcessorBE implements MultiblockAttachable {

    private AbstractMultiblock multiblock;
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
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = multiblock;
        if (multiblock != null) {
            if(multiblock.controller() != null) {
                controller = (FissionControllerBE) multiblock.controller().controllerBE();
            }
        } else {
            controller = null;
        }
        markDirty();
    }

    @Override
    public FissionControllerBE controller() {
        return controller;
    }

    @Override
    public AbstractMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    public void updateAnalogSignal() {

    }

    public void tickServer() {
        int wasFlux = irradiativeFlux;
        double wasFuel = fuelMultiplier;
        boolean wasActive = isActive;
        irradiativeFlux = 0;
        fuelMultiplier = 0;
        //allow only partial updates when irradiator boosted by torcherino
        if(lastTickTime == level.getGameTime() && level.getRandom().nextDouble() < 0.05) {
            return;
        }
        isActive = isActive();
        //upadteMultiblockConnection();
        if (isActive) {
            irradiativeFlux = controller().getIrradiativeFlux();
            FissionControllerBE.Recipe recipe1 = (FissionControllerBE.Recipe) controller().recipeInfo().recipe();
            fuelMultiplier = recipe1 != null ? recipe1.getIrradiationRate() : 0;
        }
        if(isActive && speedMultiplier() > 0) {
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            super.tickServer();
        } else {
            contentHandler().tick();
        }
        needToUpdate |= isActive != wasActive;
        if(wasFlux != irradiativeFlux || wasFuel != fuelMultiplier || needToUpdate) {
            needToUpdate = false;
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, isActive), Block.UPDATE_NEIGHBORS);
            setChanged();
        }
    }

    protected boolean isActive() {
        return controller() != null
                && !controller().isRemoved()
                && controller().isProcessing()
                && controller().efficiency > 0
                && controller().isCasingValid
                && controller().isInternalValid;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        wasUpdated = true;
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
        AbstractMultiblock mb = MultiblockHandler.get(level.dimension()).getMultiblockByPos(getBlockPos());
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
