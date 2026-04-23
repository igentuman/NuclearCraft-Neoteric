package igentuman.nc.block.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MultiblockPortBE extends NuclearCraftBE implements MultiblockAttachable {

    public MultiblockPortBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public boolean pushPull() {
        return false;
    }
}
