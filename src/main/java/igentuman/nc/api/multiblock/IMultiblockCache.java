package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** Caches block-state and block-entity lookups plus structure positions for one multiblock instance. */
public interface IMultiblockCache {
    BlockState getBlockState(Level level, BlockPos pos);

    @Nullable
    BlockEntity getBlockEntity(Level level, BlockPos pos);

    /** Mutable set of positions (as {@code BlockPos.asLong()}) belonging to the structure. */
    Set<Long> getStructurePositions();

    void invalidate(BlockPos pos);

    void clear();
}
