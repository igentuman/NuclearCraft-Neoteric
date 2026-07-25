package igentuman.nc.api.impl;

import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Map-backed multiblock cache keyed by packed positions, with per-position invalidation and NBT. */
public class MultiblockCacheImpl implements IMultiblockCache {

    private final Map<Long, BlockState> blockStateCache = new ConcurrentHashMap<>();
    private final Map<Long, BlockEntity> blockEntityCache = new ConcurrentHashMap<>();
    private final Set<Long> structurePositions = ConcurrentHashMap.newKeySet();

    @Override
    public BlockState getBlockState(Level level, BlockPos pos) {
        long key = pos.asLong();
        BlockState cached = blockStateCache.get(key);
        if (cached != null) return cached;
        BlockState fresh = WorldUtil.getBlockState(pos, (ServerLevel) level);
        blockStateCache.put(key, fresh);
        return fresh;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(Level level, BlockPos pos) {
        long key = pos.asLong();
        BlockEntity cached = blockEntityCache.get(key);
        if (cached != null) return cached;
        BlockEntity fresh = WorldUtil.getBlockEntity(pos, (ServerLevel) level);
        if (fresh != null) blockEntityCache.put(key, fresh);
        return fresh;
    }

    @Override
    public Set<Long> getStructurePositions() {
        return structurePositions;
    }

    @Override
    public void invalidate(BlockPos pos) {
        long key = pos.asLong();
        blockStateCache.remove(key);
        blockEntityCache.remove(key);
    }

    @Override
    public void clear() {
        blockStateCache.clear();
        blockEntityCache.clear();
        structurePositions.clear();
    }
}
