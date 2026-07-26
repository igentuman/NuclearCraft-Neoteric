package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

    Set<Long> getStructurePositions();

    default void setAABB(BlockPos min, BlockPos max) {}

    default boolean hasAABB() { return false; }

    default long aabbMinPacked() { return 0; }

    default long aabbMaxPacked() { return 0; }

    void invalidate(BlockPos pos);

    void clear();

    void saveNbt(CompoundTag tag, HolderLookup.Provider registries);

    void loadNbt(CompoundTag tag, HolderLookup.Provider registries);
}
