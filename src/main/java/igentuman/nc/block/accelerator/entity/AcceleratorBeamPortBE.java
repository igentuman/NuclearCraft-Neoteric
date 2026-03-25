package igentuman.nc.block.accelerator.entity;

import igentuman.api.platform.NCLevels;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.MultiblockPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.IParticleStackHandler;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.util.Equations;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.Objects;

import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;

public class AcceleratorBeamPortBE extends MultiblockPortBE {

    public static String NAME = "accelerator_beam_port";
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte comparatorMode = SignalSource.ENERGY;
    @NBTField
    public BlockPos controllerPos;
    protected AbstractAcceleratorMultiblock multiblock;
    public boolean refreshCacheFlag = true;
    public byte validationRuns = 0;
    public LinearAcceleratorControllerBE controller;

    public AcceleratorBeamPortBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }
    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (AbstractAcceleratorMultiblock) multiblock;
        setChanged();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        syncToTrackingClients();
    }

    @Override
    public AbstractAcceleratorMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        super.tickServer();
        if(getMultiblock() == null || controller() == null) return;
        int wasSignal = analogSignal;
        boolean updated = false;
        sendOutPower();
        if(controllerPos == null) {
            controllerPos = controller().getBlockPos();
            updated = true;
            setChanged();
        }
        if(hasRedstoneSignal()) {
            controller().controllerEnabled = true;
        }

        updateAnalogSignal();

        updated = wasSignal != analogSignal || updated;
        switch (comparatorMode) {
            case SignalSource.FREQUENCY -> controller().heatRate = analogSignal;
            case SignalSource.TRANSFORMATION_ENERGY_RATE -> controller().heatRate = analogSignal/15*100;
        }
        Direction dir = getFacing();

        if(fluidHandler() != null) {
            updated = fluidHandler().pullFluids(dir, false, worldPosition) || updated;
        }

        if(updated) {
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            syncToTrackingClients();
        }
    }

    protected void sendOutPower() {
        for (Direction direction : Direction.values()) {
            pullEnergyFromSide(direction);
        }
    }

    private void updateAnalogSignal() {
        if(controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (comparatorMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15 / controller().energyStorage().getMaxEnergyStored());
                break;
            case SignalSource.PROGRESS:
                analogSignal = (byte) (getProgress()/100D * 15);
                break;
            case SignalSource.ITEMS:
                analogSignal = (byte) (controller().contentHandler().itemHandler.getStackInSlot(0).getCount() * 15 / controller().contentHandler().itemHandler.getStackInSlot(0).getMaxStackSize());
                break;
            case SignalSource.FREQUENCY:
                analogSignal = (byte) (Math.max(1, getRedstoneSignal()));
                break;
            case SignalSource.TRANSFORMATION_ENERGY_RATE:
                analogSignal = (byte) (Math.max(1, getRedstoneSignal()));
                break;
        }
    }

    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler().fluidHandler;
    }



    @Override
    public AbstractAcceleratorControllerBE controller() {
        if(NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if(getLevel().isClientSide && controllerPos != null) {
            return (AbstractAcceleratorControllerBE) NCLevels.getExistingBlockEntity(getLevel(), controllerPos);
        }
        try {
            return (AbstractAcceleratorControllerBE) getMultiblock().controller().controllerBE();
        } catch (NullPointerException e) {
            if(controllerPos != null) {
                return (AbstractAcceleratorControllerBE) NCLevels.getExistingBlockEntity(getLevel(), controllerPos);
            }
            return null;
        }
    }

    public int getEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        if(controller() == null) return 0;
        return controller().heatRate;
    }

    public void toggleComparatorMode() {
        comparatorMode++;
        if(comparatorMode > SignalSource.TRANSFORMATION_ENERGY_RATE) {
            comparatorMode = SignalSource.ENERGY;
        }
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        syncToTrackingClients();
    }

    public FluidTank getFluidTank(int i) {
        if(controller() == null) return null;
        return controller().getFluidTank(i);
    }

    public double getProgress() {
        if(controller() == null) return 0;
        return controller().recipeInfo().getProgress();
    }

    public void extractParticle(ParticleStack particleStack) {
        if(controller() == null || particleStack == null || particleStack.getAmount() <= 0) return;
        
        Direction facing = getFacing();
        BlockPos currentPos = worldPosition.relative(facing);
        int maxDistance = 16;
        
        for (int distance = 0; distance < maxDistance; distance++) {

            BlockState blockState = level.getBlockState(currentPos);
            if (blockState.is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                currentPos = currentPos.relative(facing);
                continue;
            }
            
            if (level.getBlockEntity(currentPos) instanceof AcceleratorBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance-1, particleStack));
                        IParticleStackHandler handler = targetPort.controller().particleStorage;
                        if (handler != null) {
                            handler.reciveParticle(facing.getOpposite(), particleStack);
                        }
                    }
                }
                break;
            } else if (level.getBlockEntity(currentPos) instanceof TargetChamberBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance-1, particleStack));
                        IParticleStackHandler tcHandler = targetPort.controller().particleStorage;
                        if (tcHandler != null) {
                            tcHandler.reciveParticle(facing.getOpposite(), particleStack);
                        }
                    }
                }
                break;
            } else {
                break;
            }
        }
    }


    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte MASS = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
        public static final byte FREQUENCY = 5;
        public static final byte TRANSFORMATION_ENERGY_RATE = 6;
    }
}
