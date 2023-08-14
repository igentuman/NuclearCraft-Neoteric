package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        if(refreshCacheFlag || validationRuns < 2) {
            validationRuns++;
            if(refreshCacheFlag) {
                attachedToFuelCell = false;
                validationRuns = 0;
            }
            for (Direction dir : Direction.values()) {
                BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                if (be instanceof FissionFuelCellBE) {
                    attachedToFuelCell = true;
                    break;
                }
            }
        }
        return attachedToFuelCell;
    }
}
