package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiblockHandler {

    public static final ConcurrentHashMap<ResourceKey<Level>, MultiblockHandler> instances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AbstractNCMultiblock> multiblocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<String>> chunkCache = new ConcurrentHashMap<>();
    private final Set<String> toRemove = Collections.synchronizedSet(new HashSet<>());
    public final Set<Long> ignoreUpdate = Collections.synchronizedSet(new HashSet<>());

    private MultiblockHandler() {
    }

    public static MultiblockHandler get(ResourceKey<Level> dimension) {
        if (instances.containsKey(dimension)) {
            return instances.get(dimension);
        }
        MultiblockHandler handler = new MultiblockHandler();
        instances.put(dimension, handler);
        return handler;
    }

    public void addMultiblock(AbstractNCMultiblock multiblock) {
        if (multiblock.controller() == null) {
            throw new IllegalArgumentException("Multiblock controller is null");
        }
        if (!multiblocks.containsKey(multiblock.getId())) {
            multiblocks.put(multiblock.getId(), multiblock);
        } else {
            multiblocks.putIfAbsent(multiblock.getId(), multiblock);
        }
        addToChunkCache(multiblock);
    }

    private void addToChunkCache(AbstractNCMultiblock multiblock) {
        long chunkPos = multiblock.getChunk().toLong();
        if (!chunkCache.containsKey(chunkPos)) {
            chunkCache.put(chunkPos, Collections.synchronizedList(new ArrayList<>()));
        }
        List<String> list = chunkCache.get(chunkPos);
        if (!list.contains(multiblock.getId())) {
            list.add(multiblock.getId());
        }
    }

    private void removeFromChunkCache(String id) {
        for (List<String> list : chunkCache.values()) {
            list.remove(id);
        }
        List<Long> chunkPosSet = chunkCache.keySet().stream().toList();
        for (long chunkPos : chunkPosSet) {
            if (!chunkCache.containsKey(chunkPos)) {
                continue;
            }
            List<String> list = chunkCache.get(chunkPos);
            if (list.isEmpty()) {
                chunkCache.remove(chunkPos);
            }
        }
    }

    public void addMultiblock(AbstractNCMultiblock multiblock, boolean force) {
        if (multiblocks.containsKey(multiblock.getId()) && force) {
            multiblocks.remove(multiblock.getId());
        }
        addMultiblock(multiblock);
    }

    public void addIgnoreToUpdate(BlockPos blockPos) {
        if (blockPos != null) {
            ignoreUpdate.add(blockPos.asLong());
        }
    }

    public void trackBlockChange(BlockPos pos) {
        if (pos == null || multiblocks.isEmpty()) {
            return;
        }
        if (ignoreUpdate.remove(pos.asLong())) {
            return;
        }
        //Iterate chunk cache first for better performance
        long chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4).toLong();
        if (chunkCache.containsKey(chunkPos)) {
            List<String> list = chunkCache.get(chunkPos);
            for (String id : list) {
                AbstractNCMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null) {
                    continue;
                }
                if (multiblock.onBlockChange(pos)) {
                    return;
                }
            }
        }
        List<AbstractNCMultiblock> tmp = new ArrayList<>(multiblocks.values());
        for(AbstractNCMultiblock multiblock: tmp) {
            if (multiblock == null) {
                continue;
            }
            if (multiblock.onBlockChange(pos)) {
                break;
            }
        }
    }

    public void tick(Level level) {
        Set<String> tmp = multiblocks.keySet();
        for(String id: tmp) {
            AbstractNCMultiblock multiblock = multiblocks.get(id);
            if (multiblock == null || multiblock.controller() == null || multiblock.controller().controllerBE() == null) {
                toRemove.add(id);
                continue;
            }
            if (multiblock.isLoaded()) {
                if (multiblock.controller().controllerBE().isRemoved()) {
                    toRemove.add(id);
                    continue;
                }
                multiblock.tick();
            }
        }
        if (!toRemove.isEmpty()) {
            for(String id: toRemove) {
                multiblocks.remove(id);
                removeFromChunkCache(id);
            }
            toRemove.clear();
        }
    }

    public void removeMultiblock(AbstractNCMultiblock multiblock) {
        multiblocks.remove(multiblock.getId());
        removeFromChunkCache(multiblock.getId());
    }

    public boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
        if (multiblocks.isEmpty()) {
            return false;
        }
        //Iterate chunk cache first for better performance
        long chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4).toLong();
        if (chunkCache.containsKey(chunkPos)) {
            List<String> list = chunkCache.get(chunkPos);
            for (String id : list) {
                AbstractNCMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null) {
                    continue;
                }
                if (multiblock.checkAttachmentToBlock(toCheck, level, pos, dir)) {
                    return true;
                }
            }
        }
        for(AbstractNCMultiblock multiblock: multiblocks.values()) {
            if (multiblock == null) {
                continue;
            }
            if (multiblock.checkAttachmentToBlock(toCheck, level, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    public AbstractNCMultiblock getMultiblockByPos(BlockPos pos) {
        if (multiblocks.isEmpty()) {
            return null;
        }
        long chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4).toLong();
        if (chunkCache.containsKey(chunkPos)) {
            List<String> list = chunkCache.get(chunkPos);
            for (String id : list) {
                AbstractNCMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null) {
                    continue;
                }
            }
        }

        // If not found in chunk cache, check all multiblocks as fallback
        for (AbstractNCMultiblock multiblock : multiblocks.values()) {
            if (multiblock != null && multiblock.containsPos(pos)) {
                return multiblock;
            }
        }

        return null;
    }
}
