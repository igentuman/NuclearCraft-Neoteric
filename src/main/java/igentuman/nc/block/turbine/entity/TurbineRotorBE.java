package igentuman.nc.block.turbine.entity;

import igentuman.nc.block.turbine.TurbineBearingBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.util.ModUtil.isCreateLoaded;

public class TurbineRotorBE extends TurbineBE {
    @NBTField(syncAlways = true)
    public BlockPos controllerPos = BlockPos.ZERO;

    private float rotation = 0;
    private float scaling = -1;
    public static String NAME = "turbine_rotor_shaft";
    public boolean connectedToBearing = false;
    public TurbineRotorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public void updateBearingConnection() {
        connectedToBearing = false;
        Direction facing = getBlockState().getValue(TurbineRotorBlock.FACING);
        for(Direction dir: List.of(facing, facing.getOpposite())) {
            BlockEntity be = getLevel().getExistingBlockEntity(getBlockPos().relative(dir));
            BlockState bs = getLevel().getBlockState(getBlockPos().relative(dir));
            if(be instanceof TurbineRotorBE rotor) {
                connectedToBearing = rotor.hasBearingConnection(dir);
                if(connectedToBearing) break;
            }
            if(bs.getBlock() instanceof TurbineBearingBlock) {
                connectedToBearing = true;
                break;
            }
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        saveFullTagData(tag);
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        readTagData(tag);
    }

    @Override
    public void tickServer() {
        //Disallow boosters like torcherino
        if(lastTickTime == currentTick) {
            return;
        }
        lastTickTime = currentTick;
        super.tickServer();

        BlockPos wasPos = controllerPos;
        if(wasPos != getControllerPos()) {
            controllerPos = getControllerPos();
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, getRotationSpeed() > 0));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, getRotationSpeed() > 0), Block.UPDATE_NEIGHBORS);
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
        BlockEntity be = getLevel().getExistingBlockEntity(getBlockPos().relative(dir));
        BlockState bs = getLevel().getBlockState(getBlockPos().relative(dir));
        if(be instanceof TurbineRotorBE rotor) {
            connectedToBearing = rotor.hasBearingConnection(dir);
        }
        if(bs.getBlock() instanceof TurbineBearingBlock) {
            connectedToBearing = true;
        }
        return connectedToBearing;
    }

    public TurbineControllerBE getController() {
        if(controllerPos == BlockPos.ZERO) return controller();
        BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
        if(be instanceof TurbineControllerBE controller) {
            return controller;
        }
        return controller();
    }

    public float getRotationSpeed() {
        if(level == null) return 0;
        TurbineControllerBE controller = getController();
        rotation = 0;
        if(controller instanceof TurbineControllerBE) {
            if(controller.isRemoved()) {
                return rotation;
            }
            rotation = controller.getRotationSpeed();
        }
        return rotation;
    }

    public int getAttachedBlades() {
        if(level == null) return 8;
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
        if(level == null) return true;
        if(getController() == null) {
            return false;
        }
        if(getController() instanceof TurbineControllerBE && getController().isRemoved()) {
            return false;
        }
        return getController().controller().isInternalValid && getController().controller().isCasingValid;
    }

    public float getScaling() {
        if(level == null) return 1f;
        if(level.getDayTime() % 20 != 0 && scaling != -1) {
            return scaling;
        }
        TurbineControllerBE controller = getController();
        if(controller == null) return 1f;
        BlockPos steamPos = controller.getBlockPosForSteam();
        Direction facing = getBlockState().getValue(TurbineRotorBlock.FACING);
        Direction.Axis axis = facing.getAxis();
        int steam = axis.choose(steamPos.getX(), steamPos.getY(), steamPos.getZ());
        int self = Math.abs(axis.choose(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()) - steam);
        int dMin = self;
        int dMax = self;
        int count = 1;
        for(Direction dir: List.of(facing, facing.getOpposite())) {
            BlockPos p = getBlockPos();
            for(int i = 1; i < Math.max(Math.max(controller.height, controller.width), controller.depth); i++) {
                p = p.relative(dir);
                if(!(getLevel().getBlockEntity(p) instanceof TurbineRotorBE)) break;
                count++;
                int d = Math.abs(axis.choose(p.getX(), p.getY(), p.getZ()) - steam);
                if(d < dMin) dMin = d;
                if(d > dMax) dMax = d;
            }
        }
        if(count <= 1 || dMax == dMin) return 1f;
        float t = (float)(self - dMin) / (dMax - dMin);
        scaling = 0.3f + t * 0.7f;
        return scaling;
    }
}
