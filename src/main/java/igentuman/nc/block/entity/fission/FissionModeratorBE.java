package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FissionModeratorBE extends FissionBE {
    public static String NAME = "fission_moderator";
    public FissionModeratorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {   }

    @Override
    public boolean isAttachedToFuelCell() {
        return isDirectlyAttachedToFuelCell(worldPosition);
    }
}
