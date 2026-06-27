package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Cache layer over world block-state/block-entity lookups for a single multiblock instance.
 * Keys stored internally as {@link BlockPos#asLong()} to avoid BlockPos allocation per lookup.
 * Invalidated on relevant block-change events by {@code MultiblockHandler}.
 */
public interface IMultiblockCache {
    BlockState getBlockState(Level level, BlockPos pos);

    @Nullable
    BlockEntity getBlockEntity(Level level, BlockPos pos);

    /** Mutable set of positions (as {@code BlockPos.asLong()}) belonging to the structure. */
    Set<Long> getStructurePositions();

    void invalidate(BlockPos pos);

    void clear();

    /** Persist cache state (notably structure positions) for world-save. */
    void saveNbt(CompoundTag tag, HolderLookup.Provider registries);

    /** Restore cache state previously written by {@link #saveNbt}. */
    void loadNbt(CompoundTag tag, HolderLookup.Provider registries);
}
