package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.turbine.TurbineRotorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;

import java.util.List;

public class TurbineBearingBE extends TurbineBE {
    public static String NAME = "turbine_bearing";
    public TurbineBearingBE() {
        this(BlockPos.ZERO, null);
    }
    public TurbineBearingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }
}
