package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.turbine.TurbineRotorBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;

import java.util.Arrays;
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
        for(Direction dir: Arrays.asList(facing, facing.getOpposite())) {
            TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
            if(be instanceof TurbineRotorBE) {
                TurbineRotorBE rotor = (TurbineRotorBE) be;
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
        TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
        if(be instanceof TurbineRotorBE) {
            TurbineRotorBE rotor = (TurbineRotorBE) be;
            connectedToBearing = rotor.hasBearingConnection(dir);
        }
        if(be instanceof TurbineBearingBE) {
            connectedToBearing = true;
        }
        return connectedToBearing;
    }
}
