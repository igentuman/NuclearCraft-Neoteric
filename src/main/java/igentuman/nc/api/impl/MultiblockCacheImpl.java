package igentuman.nc.api.impl;

import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiblockCacheImpl implements IMultiblockCache {

    private final Map<Long, BlockState> blockStateCache = new HashMap<>();
    private final Map<Long, BlockEntity> blockEntityCache = new HashMap<>();
    private final Set<Long> structurePositions = new HashSet<>();

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
        if (blockEntityCache.containsKey(key)) {
            return blockEntityCache.get(key);
        }
        BlockEntity fresh = WorldUtil.getBlockEntity(pos, (ServerLevel) level);
        blockEntityCache.put(key, fresh);
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

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        long[] arr = new long[structurePositions.size()];
        int i = 0;
        for (long p : structurePositions) arr[i++] = p;
        tag.putLongArray("structurePositions", arr);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        structurePositions.clear();
        if (tag.contains("structurePositions")) {
            for (long p : tag.getLongArray("structurePositions")) structurePositions.add(p);
        }
    }
}
