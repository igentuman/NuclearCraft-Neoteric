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

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiblockCacheImpl implements IMultiblockCache {

    private final Map<Long, BlockState> blockStateCache = new ConcurrentHashMap<>();
    private final Map<Long, BlockEntity> blockEntityCache = new ConcurrentHashMap<>();
    private final Set<Long> structurePositions = ConcurrentHashMap.newKeySet();

    private volatile boolean hasAABB;
    private volatile long aabbMin;
    private volatile long aabbMax;

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
        if (hasAABB) {
            return new AABBPositionSet();
        }
        return structurePositions;
    }

    @Override
    public void setAABB(BlockPos min, BlockPos max) {
        aabbMin = min.asLong();
        aabbMax = max.asLong();
        hasAABB = true;
        structurePositions.clear();
    }

    @Override
    public boolean hasAABB() {
        return hasAABB;
    }

    @Override
    public long aabbMinPacked() {
        return aabbMin;
    }

    @Override
    public long aabbMaxPacked() {
        return aabbMax;
    }

    public void clearAABB() {
        hasAABB = false;
        aabbMin = 0;
        aabbMax = 0;
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
        clearAABB();
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if (hasAABB) {
            tag.putBoolean("aabb", true);
            tag.putLong("aabbMin", aabbMin);
            tag.putLong("aabbMax", aabbMax);
        } else {
            long[] arr = new long[structurePositions.size()];
            int i = 0;
            for (long p : structurePositions) arr[i++] = p;
            tag.putLongArray("structurePositions", arr);
        }
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        structurePositions.clear();
        clearAABB();
        if (tag.getBoolean("aabb")) {
            aabbMin = tag.getLong("aabbMin");
            aabbMax = tag.getLong("aabbMax");
            hasAABB = true;
        } else if (tag.contains("structurePositions")) {
            for (long p : tag.getLongArray("structurePositions")) structurePositions.add(p);
        }
    }

    /** Lazy, read-only view that computes all packed positions within the AABB on iteration.
     *  Mutations are limited to {@code clear()} (delegates to {@link #clearAABB()}). */
    private final class AABBPositionSet extends AbstractSet<Long> {

        @Override
        public Iterator<Long> iterator() {
            if (!hasAABB) return Collections.emptyIterator();
            return new AABBIterator(aabbMin, aabbMax);
        }

        @Override
        public int size() {
            if (!hasAABB) return 0;
            BlockPos min = BlockPos.of(aabbMin);
            BlockPos max = BlockPos.of(aabbMax);
            return (max.getX() - min.getX() + 1)
                    * (max.getY() - min.getY() + 1)
                    * (max.getZ() - min.getZ() + 1);
        }

        @Override
        public boolean isEmpty() {
            return !hasAABB;
        }

        @Override
        public void clear() {
            clearAABB();
        }

        @Override
        public boolean contains(Object o) {
            if (!hasAABB || o == null) return false;
            long key = (Long) o;
            BlockPos p = BlockPos.of(key);
            BlockPos min = BlockPos.of(aabbMin);
            BlockPos max = BlockPos.of(aabbMax);
            return p.getX() >= min.getX() && p.getX() <= max.getX()
                    && p.getY() >= min.getY() && p.getY() <= max.getY()
                    && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
        }
    }

    /** Iterates every (x, y, z) in [min, max] inclusive, yielding packed longs. */
    private static final class AABBIterator implements Iterator<Long> {
        private final int minX, minY, minZ, maxX, maxY, maxZ;
        private int x, y, z;

        AABBIterator(long minPacked, long maxPacked) {
            BlockPos min = BlockPos.of(minPacked);
            BlockPos max = BlockPos.of(maxPacked);
            this.minX = min.getX();
            this.minY = min.getY();
            this.minZ = min.getZ();
            this.maxX = max.getX();
            this.maxY = max.getY();
            this.maxZ = max.getZ();
            this.x = minX;
            this.y = minY;
            this.z = minZ;
        }

        @Override
        public boolean hasNext() {
            return y <= maxY;
        }

        @Override
        public Long next() {
            if (y > maxY) throw new NoSuchElementException();
            long result = BlockPos.asLong(x, y, z);
            if (++x > maxX) {
                x = minX;
                if (++z > maxZ) {
                    z = minZ;
                    y++;
                }
            }
            return result;
        }
    }
}
