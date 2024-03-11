package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class FissionModeratorBE extends FissionBE {
    public static String NAME = "fission_moderator";
    public FissionModeratorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
    }

    @Override
    public boolean isAttachedToFuelCell() {
        return isDirectlyAttachedToFuelCell(worldPosition);
    }
}
