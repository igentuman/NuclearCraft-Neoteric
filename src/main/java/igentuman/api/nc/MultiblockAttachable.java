package igentuman.api.nc;

import igentuman.nc.multiblock.AbstractNCMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiblockAttachable<M extends AbstractNCMultiblock, BE extends BlockEntity> {

    /**
     * Sets the multiblock instance for this block
     * @param multiblock
     */
    void setMultiblock(M multiblock);

    /**
     * Returns the controller block entity
     */
    BE controller();

    /**
     * Returns the instance reference for this block
     */
    M getMultiblock();

    /**
     * Returns if the block updates can invalidate multiblock cache and initialize revalidation
     */
    boolean canInvalidateCache();

    /**
     * Called when nearby blocks change
     */
    default void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(getMultiblock() != null) {
            getMultiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
