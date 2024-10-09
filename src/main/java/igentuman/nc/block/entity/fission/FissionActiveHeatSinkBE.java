package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FissionActiveHeatSinkBE extends FissionHeatSinkBE {
    public static String NAME = "fission_active_heat_sink";

    public FissionActiveHeatSinkBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

}
