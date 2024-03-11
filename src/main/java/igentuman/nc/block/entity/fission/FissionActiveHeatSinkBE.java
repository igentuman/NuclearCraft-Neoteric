package igentuman.nc.block.entity.fission;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class FissionActiveHeatSinkBE extends FissionHeatSinkBE {
    public static String NAME = "fission_active_heat_sink";

    public FissionActiveHeatSinkBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

}
