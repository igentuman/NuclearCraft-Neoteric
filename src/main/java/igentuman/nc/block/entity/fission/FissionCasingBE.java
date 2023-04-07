package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FissionCasingBE extends FissionBE {
    public static String NAME = "fission_casing";
    public FissionCasingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {   }
}
