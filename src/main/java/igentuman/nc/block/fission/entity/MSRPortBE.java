package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.MSRMultiblock;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;

public class MSRPortBE extends MultiblockPortBE {

    public static final String NAME = "msr_port";

    @NBTField
    public BlockPos controllerPos = BlockPos.ZERO;
    @NBTField
    public boolean connected = false;
    protected long lastTickTime = 0;
    protected MSRMultiblock multiblock;
    protected MSRControllerBE controller;

    public MSRPortBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public MSRMultiblock getMultiblock() {
        return multiblock;
    }

    private boolean updateController() {
        boolean result = false;
        if (controller != controller()) {
            controller = controller();
            controllerPos = BlockPos.ZERO;
            result = true;
        }
        if (controller != null && !controller.getBlockPos().equals(controllerPos)) {
            controllerPos = new BlockPos(controller.getBlockPos());
            result = true;
        }
        return result;
    }

    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) return;

        boolean wasConnected = connected;
        if (lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();

        boolean updated = updateController();

        if (currentTick % 20 == 0 && controller() != null) {
            pushPull();
        }

        connected = getMultiblock() != null && getMultiblock().isFormed();
        if (updated || wasConnected != connected) {
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
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

    protected ItemCapabilityHandler itemHandler() {
        MSRControllerBE ctrl = controller();
        return ctrl == null ? null : ctrl.contentHandler().itemHandler;
    }

    protected FluidCapabilityHandler fluidHandler() {
        MSRControllerBE ctrl = controller();
        return ctrl == null ? null : ctrl.contentHandler().fluidHandler;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
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

    @Override
    public MSRControllerBE controller() {
        if (NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if (controller == null && getLevel().isClientSide && controllerPos != null && !controllerPos.equals(BlockPos.ZERO)) {
            BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
            if (be instanceof MSRControllerBE controllerBe) {
                controller = controllerBe;
                return controller;
            }
        }
        try {
            BlockEntity be = getMultiblock().controller().controllerBE();
            if (be instanceof MSRControllerBE controllerBe) {
                controller = controllerBe;
                return controller;
            }
        } catch (NullPointerException e) {
            if (controllerPos != null && !controllerPos.equals(BlockPos.ZERO)) {
                BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
                if (be instanceof MSRControllerBE controllerBe) {
                    controller = controllerBe;
                }
            }
        }
        return controller;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        MSRControllerBE ctrl = controller();
        if (ctrl == null) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ctrl.getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return ctrl.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }
}
