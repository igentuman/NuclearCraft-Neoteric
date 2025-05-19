package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        } else {
            multiblocks.putIfAbsent(multiblock.getId(), multiblock);
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
        for(long packedPos: changedBlocks) {
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
                    if (multiblock.onBlockChange(pos)) {
                        return;
                    }
                }
            }
            List<AbstractMultiblock> tmp = new ArrayList<>(multiblocks.values());
            for(AbstractMultiblock multiblock: tmp) {
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

    public void trackBlockChange(BlockPos pos) {
        if (pos == null || multiblocks.isEmpty()) {
            return;
        }
        if (ignoreUpdate.contains(pos.asLong())) {
            return;
        }
        if(!changedBlocks.contains(pos.asLong())) {
            changedBlocks.add(pos.asLong());
        }
    }

    /**
     * Ticks all multiblocks in this handler
     * @param level The level to use for operations (can be null, will use controller's level if null)
     */
    private final ConcurrentHashMap<String, CompletableFuture<Void>> tickFutures = new ConcurrentHashMap<>();
    
    // Track the number of multiblocks that need validation this tick
    private int validationsThisTick = 0;
    // Maximum number of multiblocks to validate per tick
    private static final int MAX_VALIDATIONS_PER_TICK = 5;
    // Track multiblocks that need validation but were deferred
    private final Set<String> deferredValidations = Collections.synchronizedSet(new HashSet<>());
    
    public void tick(Level level) {
        trackAllChanges();
        Set<String> tmp = multiblocks.keySet();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        validationsThisTick = 0;
        
        // First process any deferred validations from previous ticks
        if (!deferredValidations.isEmpty()) {
            Iterator<String> iterator = deferredValidations.iterator();
            while (iterator.hasNext() && validationsThisTick < MAX_VALIDATIONS_PER_TICK) {
                String id = iterator.next();
                iterator.remove();
                
                AbstractMultiblock multiblock = multiblocks.get(id);
                if (multiblock == null || !multiblock.isValidForTicking()) {
                    toRemove.add(id);
                    continue;
                }
                
                if (multiblock.hasToRefresh && multiblock.isLoaded()) {
                    scheduleMultiblockTick(id, multiblock, futures);
                    validationsThisTick++;
                }
            }
        }
        
        // Process regular ticks for all multiblocks
        for(String id: tmp) {
            AbstractMultiblock multiblock = multiblocks.get(id);
            if (multiblock == null || !multiblock.isValidForTicking()) {
                toRemove.add(id);
                continue;
            }
            
            // Check if there's an existing future for this multiblock and if it's still running
            CompletableFuture<Void> existingFuture = tickFutures.get(id);
            if (existingFuture != null && !existingFuture.isDone()) {
                // Previous tick is still running, skip this multiblock
                continue;
            }
            
            if (multiblock.isLoaded()) {
                if (multiblock.controller().controllerBE().isRemoved()) {
                    toRemove.add(id);
                    continue;
                }
                
                // If this multiblock needs validation and we've hit our limit, defer it
                if (multiblock.hasToRefresh && validationsThisTick >= MAX_VALIDATIONS_PER_TICK) {
                    deferredValidations.add(id);
                    continue;
                }
                
                // If this multiblock needs validation, increment our counter
                if (multiblock.hasToRefresh) {
                    validationsThisTick++;
                }
                
                scheduleMultiblockTick(id, multiblock, futures);
            }
        }
        
        // Clean up completed futures
        tickFutures.entrySet().removeIf(entry -> entry.getValue().isDone());
        
        if (!toRemove.isEmpty()) {
            for(String id: toRemove) {
                multiblocks.remove(id);
                removeFromChunkCache(id);
                tickFutures.remove(id);
                deferredValidations.remove(id);
            }
            toRemove.clear();
        }
        
        // Log if we have many deferred validations
        if (deferredValidations.size() > 20) {
            debugLog("Warning: " + deferredValidations.size() + " multiblocks waiting for validation");
        }
    }
    
    private void scheduleMultiblockTick(String id, AbstractMultiblock multiblock, List<CompletableFuture<Void>> futures) {
        // Run the tick asynchronously using the executor
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                multiblock.tick();
            } catch (Exception e) {
                debugLog("Error during async multiblock tick for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, MultiblockExecutorManager.getExecutor());
        
        tickFutures.put(id, future);
        futures.add(future);
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
        multiblocks.remove(id);
        removeFromChunkCache(id);
        tickFutures.remove(id);
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
        for(AbstractMultiblock multiblock: multiblocks.values()) {
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
        tickFutures.clear();
    }
}
