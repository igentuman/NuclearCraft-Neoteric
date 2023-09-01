package igentuman.nc.multiblock;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInfo {
    protected final BlockState state;
    protected final BlockEntity entity;

    public BlockInfo(BlockState state, BlockEntity entity) {
        this.state = state;
        this.entity = entity;
    }
}
