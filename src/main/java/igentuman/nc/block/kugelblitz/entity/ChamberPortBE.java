package igentuman.nc.block.kugelblitz.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BE;

public class ChamberPortBE extends MultiblockPortBE {

    public static String NAME = "chamber_port";

    protected KugelblitzMultiblock multiblock;

    public ChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        super(KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(this.multiblock == multiblock) {
            return;
        }
        this.multiblock = (KugelblitzMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = this.multiblock.controller().controllerBE();
            markDirty();
        }
    }

    @Override
    public KugelblitzMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;

        boolean updated = updateController();
        int wasSignal = analogSignal;
        if(getMultiblock() != null && isConnectedToController()) {
            sendOutPower();
        }
        if(hasRedstoneSignal() && controller() != null) {
            controller().controllerEnabled = true;
        }

        if(isConnectedToController()) {
            updateAnalogSignal();
            updated |= wasSignal != analogSignal;
            switch (redstoneMode) {
                case SignalSource.FREQUENCY -> controller().frequency = analogSignal;
                case SignalSource.TRANSFORMATION_ENERGY_RATE -> controller().energyConvertionRate = analogSignal/15*100;
            }
            updated |= pushPull();
        }

        updateIfNeeded(updated);
    }

    @Override
    public ChamberTerminalBE controller() {
        return (ChamberTerminalBE) super.controller();
    }

    public void updateAnalogSignal() {
        if(controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (redstoneMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15 / controller().energyStorage().getMaxEnergyStored());
                break;
            case SignalSource.MASS:
                analogSignal = (byte) (controller().mass * 15 / BlackHoleBE.MAX_MASS);
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

    public void toggleComparatorMode() {
        redstoneMode++;
        if(redstoneMode > SignalSource.TRANSFORMATION_ENERGY_RATE) {
            redstoneMode = SignalSource.ENERGY;
        }
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public double getProgress() {
        if(controller() == null) return 0;
        return controller().recipeInfo().getProgress();
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
