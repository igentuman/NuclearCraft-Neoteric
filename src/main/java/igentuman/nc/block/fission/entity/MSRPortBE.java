package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.MSRMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.currentTick;

public class MSRPortBE extends MultiblockPortBE {

    public static final String NAME = "msr_port";
    protected MSRMultiblock multiblock;

    public MSRPortBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
        redstoneMode = SignalSource.TEMPERATURE;
    }

    public MSRMultiblock getMultiblock() {
        return multiblock;
    }

    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) return;

        boolean wasConnected = connected;
        if (lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();

        byte wasSignal = analogSignal;
        boolean updated = updateController();
        if(getMultiblock() != null && isConnectedToController()) {
            sendOutPower();
        }
        if (currentTick % 10 == 0) {
            pushPull();
            if (isConnectedToController()) {
                updateAnalogSignal();
            }
        }
        updated |= wasSignal != analogSignal;

        connected = getMultiblock() != null && getMultiblock().isFormed();
        if (updated || wasConnected != connected) {
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!isConnectedToController()) return LazyOptional.empty();
        return controller().getCapability(cap, side);
    }

    @Override
    public void updateAnalogSignal() {
        if (controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (redstoneMode) {
            case SignalSource.DEPLETION ->
                    analogSignal = (byte) Math.max(0, Math.min(15, (int) (controller().depletion * 15)));
            case SignalSource.TEMPERATURE ->
                    analogSignal = (byte) Math.max(0, Math.min(15, (int) (controller().temperature / 2000.0 * 15)));
        }
    }

    public void toggleRedstoneMode() {
        redstoneMode++;
        if (redstoneMode > SignalSource.DEPLETION) {
            redstoneMode = SignalSource.TEMPERATURE;
        }
        analogSignal = 0;
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public static class SignalSource {
        public static final byte TEMPERATURE = 1;
        public static final byte DEPLETION = 2;
    }

    @Override
    public MSRControllerBE controller() {
        return (MSRControllerBE) super.controller();
    }

    public void tickClient() {
    }

    @Override
    public boolean pushPull() {
        boolean pushed = false;
        if (itemHandler() != null) {
            Direction dir = getFacing();
            pushed = itemHandler().pushItems(dir, true, worldPosition);
            pushed = itemHandler().pullItems(dir, true, worldPosition) || pushed;
        }
        return pushed;
    }


    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if (this.multiblock == multiblock) {
            return;
        }
        this.multiblock = (MSRMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (MSRControllerBE) this.multiblock.controller().controllerBE();
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            markDirty();
        }
    }
}
