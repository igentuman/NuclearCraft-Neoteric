package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.turbine.TurbineRotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TurbineRotorBE extends TurbineBE {
    public static String NAME = "turbine_rotor_shaft";
    public boolean connectedToBearing = false;
    public TurbineRotorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        super.tickServer();
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
}
