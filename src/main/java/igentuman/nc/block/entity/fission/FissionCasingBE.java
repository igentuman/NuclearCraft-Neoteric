package igentuman.nc.block.entity.fission;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class FissionCasingBE extends FissionBE {
    public static String NAME = "fission_casing";
    public FissionCasingBE() {
        this(BlockPos.ZERO, null);
    }
    public FissionCasingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public final boolean isAttachedToFuelCell() {
        return true;//we don't care
    }


}
