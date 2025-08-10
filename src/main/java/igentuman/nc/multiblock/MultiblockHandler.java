package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
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
        for (long packedPos : changedBlocks) {
            BlockPos pos = BlockPos.of(packedPos);
            //Iterate chunk cache first for better performance
            long chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4).toLong();
            if (chunkCache.containsKey(chunkPos)) {
                List<String> list = chunkCache.get(chunkPos);
                for (String id : list) {
                    AbstractMultiblock multiblock = multiblocks.get(id);
                    if (multiblock == null) {
                        continue;
                    }
                    multiblock.onBlockChange(pos);
                }
            }
            List<AbstractMultiblock> tmp = new ArrayList<>(multiblocks.values());
            for (AbstractMultiblock multiblock : tmp) {
                if (multiblock == null) {
                    continue;
                }
                if (multiblock.onBlockChange(pos)) {
                    break;
                }
            }
        }
        changedBlocks.clear();
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

    /**
     * Ticks all multiblocks in this handler
     *
     * @param level The level to use for operations (can be null, will use controller's level if null)
     */
    public void tick(Level level) {
        // First process any block changes
        if (!changedBlocks.isEmpty()) {
            debugLog("Processing " + changedBlocks.size() + " block changes for multiblocks");
        }
        trackAllChanges();
        
        // Collect multiblocks that need validation
        List<String> needsValidation = new ArrayList<>();
        for (String id : multiblocks.keySet()) {
            AbstractMultiblock multiblock = multiblocks.get(id);
            if (multiblock != null && multiblock.hasToRefresh && multiblock.isLoaded() && multiblock.isValidForTicking()) {
                needsValidation.add(id);
            }
        }

        // Process all validations for this level in a single thread
        if (!needsValidation.isEmpty()) {
            debugLog("Processing validation for " + needsValidation.size() + " multiblocks");

            for (String id : needsValidation) {
                AbstractMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null || !multiblock.isValidForTicking()) {
                    toRemove.add(id);
                    continue;
                }

                try {
                    multiblock.tick(level);
                } catch (Exception e) {
                    debugLog("Error during multiblock validation for " + id + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

        }

        // Process regular ticks for all multiblocks (non-validation processing)
        for (String id : multiblocks.keySet()) {
            AbstractMultiblock multiblock = multiblocks.get(id);
            if (multiblock == null || !multiblock.isValidForTicking()) {
                toRemove.add(id);
                continue;
            }

            if (multiblock.isLoaded()) {
                if (multiblock.controller().controllerBE().isRemoved()) {
                    toRemove.add(id);
                    continue;
                }

                // Only do non-validation related tasks here
                if (!multiblock.hasToRefresh) {
                    // Fast path for already validated multiblocks
                    try {
                        multiblock.tick(level);
                    } catch (Exception e) {
                        debugLog("Error during multiblock tick for " + id + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        // Cleanup
        if (!toRemove.isEmpty()) {
            debugLog("Removing " + toRemove.size() + " invalid multiblocks: " + toRemove);
            for (String id : toRemove) {
                multiblocks.remove(id);
                removeFromChunkCache(id);
            }
            toRemove.clear();
        }
    }

    /**
     * Ticks all multiblocks in this handler using the controller's level
     * For backward compatibility
     */
    public void tick() {
        tick(null);
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
}
