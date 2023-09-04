package igentuman.nc.block.entity.fusion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FusionCasingBE extends FusionBE {
    public FusionCasingBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FusionCasingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }
}
