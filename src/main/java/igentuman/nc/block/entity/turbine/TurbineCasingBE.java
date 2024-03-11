package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.entity.fission.FissionBE;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class TurbineCasingBE extends TurbineBE {
    public static String NAME = "turbine_casing";
    public TurbineCasingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }

}
