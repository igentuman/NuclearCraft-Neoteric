package igentuman.nc.block.entity.fission;

import igentuman.nc.setup.registration.NCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FissionCasing extends FissionBE {
    public static String NAME = "fission_controller";

    public FissionCasing(BlockPos pPos, BlockState pBlockState) {
        super(NCBlocks.MULTIBLOCK_BE.get(NAME).get(), pPos, pBlockState);
    }

    public void tickClient() {
    }

    public void tickServer() {

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

}
