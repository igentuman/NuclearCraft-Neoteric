package igentuman.nc.block.entity.fusion;

import igentuman.nc.multiblock.fusion.FusionReactor;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class FusionCasingBE extends FusionBE {
    public FusionCasingBE(TileEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FusionCasingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    public FusionCasingBE() {
        super(FusionReactor.FUSION_BE.get("fusion_casing").get());
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }
}
