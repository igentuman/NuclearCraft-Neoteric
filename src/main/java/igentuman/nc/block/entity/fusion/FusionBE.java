package igentuman.nc.block.entity.fusion;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.fusion.FusionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FusionBE extends NuclearCraftBE {
    public FusionBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FusionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FusionReactor.FUSION_BE.get(name).get(), pPos, pBlockState);
    }

    public void tickServer() {
    }

    public void tickClient() {
    }
}
