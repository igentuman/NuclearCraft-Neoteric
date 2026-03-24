package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.NuclearCraft.debugLog;

public class MultiblockHandler {

    public static final ConcurrentHashMap<ResourceKey<Level>, MultiblockHandler> instances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AbstractMultiblock> multiblocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<String>> chunkCache = new ConcurrentHashMap<>();
    private final Set<String> toRemove = Collections.synchronizedSet(new HashSet<>());
    public final Set<Long> ignoreUpdate = Collections.synchronizedSet(new HashSet<>());
    public final Set<Long> changedBlocks = Collections.synchronizedSet(new HashSet<>());


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

    public static void tickMultiblock(ServerLevel level, AbstractMultiblock multiblock) {
        MultiblockHandler handler = get(level.dimension());
        if(multiblock.isMarkedForRemoval()) {
            multiblock.dispose();
            return;
        }
        try {
            handler.tick(level, multiblock);
        } catch (Exception e) {
            debugLog("Exception in multiblock tick: " + e.getMessage());
        }
    }

    public static void trackChanges(ServerLevel level) {
        try {
            MultiblockHandler.get(level.dimension()).trackAllChanges();
        } catch (Exception e) {
            debugLog("Exception in trackChanges: " + e.getMessage());
        }
    }

    public void addMultiblock(AbstractMultiblock multiblock) {
        if (multiblock.controller() == null) {
            throw new IllegalArgumentException("Multiblock controller is null");
        }
        if (!multiblocks.containsKey(multiblock.getId())) {
            multiblocks.put(multiblock.getId(), multiblock);
            debugLog("Added new multiblock: " + multiblock.getId() + " (" + multiblock.getClass().getSimpleName() + ")");
        } else {
            multiblocks.putIfAbsent(multiblock.getId(), multiblock);
            debugLog("Multiblock already exists: " + multiblock.getId());
        }
        addToChunkCache(multiblock);
    }

    private void addToChunkCache(AbstractMultiblock multiblock) {
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

    public void addMultiblock(AbstractMultiblock multiblock, boolean force) {
        if (multiblocks.containsKey(multiblock.getId()) && force) {
            debugLog("Force replacing existing multiblock: " + multiblock.getId());
            multiblocks.get(multiblock.getId()).dispose();
        }
        addMultiblock(multiblock);
    }

    public void addIgnoreToUpdate(BlockPos blockPos) {
        if (blockPos != null) {
            ignoreUpdate.add(blockPos.asLong());
        }
    }

    public void trackAllChanges() {
        ConcurrentHashMap.KeySetView<String, AbstractMultiblock> tmp = multiblocks.keySet();
        for (String id : tmp) {
            if(shouldRemove(id)) {
                removeMultiblock(multiblocks.get(id));
            }
        }
        for (long packedPos : changedBlocks) {
            BlockPos pos = BlockPos.of(packedPos);
            Collection<AbstractMultiblock> multiblockCollection = multiblocks.values();
            for (AbstractMultiblock multiblock : multiblockCollection) {
                if (multiblock == null) {
                    continue;
                }
                multiblock.onBlockChange(pos);
            }
        }
        changedBlocks.clear();
    }

    private boolean shouldRemove(String id) {
        return !multiblocks.containsKey(id)
                || multiblocks.get(id) == null
                || multiblocks.get(id).controllerBE() == null
                || multiblocks.get(id).controllerBE().isRemoved()
                ;
    }

    public void trackBlockChange(BlockPos pos, boolean force) {
        if(force) {
            ignoreUpdate.remove(pos.asLong());
        }
        trackBlockChange(pos);
    }

    public void trackBlockChange(BlockPos pos) {
        if (pos == null || multiblocks.isEmpty()) {
            return;
        }
        if (ignoreUpdate.contains(pos.asLong())) {
            return;
        }
        if (!changedBlocks.contains(pos.asLong())) {
            changedBlocks.add(pos.asLong());
            debugLog("Tracking block change at " + pos.toShortString() + " (total tracked: " + changedBlocks.size() + ")");
        }
    }

    public void tick(Level level, AbstractMultiblock multiblock) {
        if(!multiblocks.containsKey(multiblock.getId())) {
            addMultiblock(multiblock, false);
        }
        if(multiblocks.get(multiblock.getId()) != multiblock) {
            if(multiblocks.get(multiblock.getId()) != null) {
                removeMultiblock(multiblocks.get(multiblock.getId()));
            } else {
                multiblocks.remove(multiblock.getId());
            }
            multiblocks.put(multiblock.getId(), multiblock);
        }
        multiblock.tick(level);
    }

    public void removeMultiblock(AbstractMultiblock multiblock) {
        String id = multiblock.getId();
        debugLog("Removing multiblock: " + id + " (" + multiblock.getClass().getSimpleName() + ")");
        multiblocks.remove(id);
        removeFromChunkCache(id);
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
                AbstractMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null) {
                    continue;
                }
                if (multiblock.checkAttachmentToBlock(toCheck, level, pos, dir)) {
                    return true;
                }
            }
        }
        for (AbstractMultiblock multiblock : multiblocks.values()) {
            if (multiblock == null) {
                continue;
            }
            if (multiblock.checkAttachmentToBlock(toCheck, level, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    public AbstractMultiblock getMultiblockByPos(BlockPos pos) {
        if (multiblocks.isEmpty()) {
            return null;
        }
        long chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4).toLong();
        if (chunkCache.containsKey(chunkPos)) {
            List<String> list = chunkCache.get(chunkPos);
            for (String id : list) {
                AbstractMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null) {
                    continue;
                }
                if (multiblock.containsPos(pos)) {
                    return multiblock;
                }
            }
        }

        // If not found in chunk cache, check all multiblocks as fallback
        for (AbstractMultiblock multiblock : multiblocks.values()) {
            if (multiblock != null && multiblock.containsPos(pos)) {
                return multiblock;
            }
        }

        return null;
    }

    public void clear() {
        for (AbstractMultiblock multiblock : multiblocks.values()) {
            if (multiblock != null) {
                multiblock.dispose();
            }
        }
        multiblocks.clear();
        chunkCache.clear();
        toRemove.clear();
        ignoreUpdate.clear();
        changedBlocks.clear();
    }

    public void onControllerRemoved(BlockPos pos) {
        AbstractMultiblock multiblock = getMultiblockByPos(pos);
        if(multiblock != null) {
            debugLog("Controller removed at " + pos.toShortString() + " for multiblock: " + multiblock.getId());
            multiblock.onControllerRemoved();
        } else {
            debugLog("Controller removed at " + pos.toShortString() + " but no multiblock found");
        }
    }
    
    /**
     * Clears all dimension handlers and their futures.
     * Should be called when the server shuts down to prevent memory leaks.
     */
    public static void clearAll() {
        debugLog("Clearing all multiblock handlers for " + instances.size() + " dimensions");
        for (MultiblockHandler handler : instances.values()) {
            handler.clear();
        }
        instances.clear();
    }
}
