package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;

public class TurbineRotorBE extends TurbineBE {
    @NBTField
    public BlockPos controllerPos = BlockPos.ZERO;
    private float rotation = 0;

    public static String NAME = "turbine_rotor_shaft";
    public boolean connectedToBearing = false;
    public TurbineRotorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public void updateBearingConnection() {
        connectedToBearing = false;
        Direction facing = getBlockState().getValue(TurbineRotorBlock.FACING);
        for(Direction dir: List.of(facing, facing.getOpposite())) {
            BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
            if(be instanceof TurbineRotorBE rotor) {
                connectedToBearing = rotor.hasBearingConnection(dir);
                if(connectedToBearing) break;
            }
            if(be instanceof TurbineBearingBE) {
                connectedToBearing = true;
                break;
            }
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        saveTagData(tag);
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        readTagData(tag);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        BlockPos wasPos = controllerPos;
        if(wasPos != getControllerPos()) {
            controllerPos = getControllerPos();
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, getRotationSpeed() > 0));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, getRotationSpeed() > 0), Block.UPDATE_ALL);
        }
    }

    private BlockPos getControllerPos() {
        if(getController() == null) {
            return BlockPos.ZERO;
        }
        return getController().getBlockPos();
    }

    private boolean hasBearingConnection(Direction dir) {
        if(connectedToBearing) return true;
        BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
        if(be instanceof TurbineRotorBE rotor) {
            connectedToBearing = rotor.hasBearingConnection(dir);
        }
        if(be instanceof TurbineBearingBE) {
            connectedToBearing = true;
        }
        return connectedToBearing;
    }

    public TurbineControllerBE<?> getController() {
        if(controllerPos == BlockPos.ZERO) return controller();
        BlockEntity be = getLevel().getBlockEntity(controllerPos);
        if(be instanceof TurbineControllerBE<?> controller) {
            return controller;
        }
        return controller();
    }

    public float getRotationSpeed() {
        TurbineControllerBE<?> controller = getController();
        rotation = 0;
        if(controller instanceof TurbineControllerBE<?>) {
            rotation = controller.getRotationSpeed();
        }
        return rotation;
    }

    public int getAttachedBlades() {
        int blades = 0;
        Direction rotorFacing = getBlockState().getValue(TurbineRotorBlock.FACING);
        Direction facing = Direction.NORTH;
        if(rotorFacing.getAxis() != Direction.Axis.Y) {
            facing = rotorFacing.getClockWise();
        }
        for(int i = 1; i < 32; i++) {
            BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(facing, i));
            if(be instanceof TurbineBladeBE) {
                blades++;
            } else {
                break;
            }
        }

        return blades*2;
    }

    public boolean isFormed() {
        if(getController() == null) {
            return false;
        }
        if(getController().multiblock() == null) {
            return false;
        }
        if(!getController().multiblock().isFormed() || getLevel().getGameTime() % 20 == 0) {
            getController().multiblock().validate();
        }
        return getController().multiblock().isFormed();
    }
}
