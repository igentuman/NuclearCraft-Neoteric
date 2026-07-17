package igentuman.api.nc.multiblock;

import igentuman.nc.multiblock.AbstractMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiblockAttachable<M extends AbstractMultiblock, BE extends BlockEntity> {

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

    /**
     * Called when nearby blocks destroyed
     */
    default void onBlockDestroyed(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if(getMultiblock() != null) {
            getMultiblock().onBlockDestroyed(state, level, pos, explosion);
        }
    }

    void updateAnalogSignal();

}
