package igentuman.nc.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

public class BlockInfo {
    protected final BlockState state;
    protected final TileEntity entity;

    public BlockInfo(BlockState state, TileEntity entity) {
        this.state = state;
        this.entity = entity;
    }
}
