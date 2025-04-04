package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultiblockHandler {

    private static final HashMap<String, AbstractNCMultiblock> multiblocks = new HashMap<>();
    private static final HashMap<Long, List<String>> chunkCache = new HashMap<>();
    private static final List<String> toRemove = new ArrayList<>();

    public static void addMultiblock(AbstractNCMultiblock multiblock) {
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

    private static void addToChunkCache(AbstractNCMultiblock multiblock) {
        long chunkPos = multiblock.getChunk().toLong();
        if (!chunkCache.containsKey(chunkPos)) {
            chunkCache.put(chunkPos, new ArrayList<>());
        }
        List<String> list = chunkCache.get(chunkPos);
        if (!list.contains(multiblock.getId())) {
            list.add(multiblock.getId());
        }
    }

    private static void removeFromChunkCache(String id) {
        for (List<String> list : chunkCache.values()) {
            list.remove(id);
        }
        for (long chunkPos : chunkCache.keySet()) {
            List<String> list = chunkCache.get(chunkPos);
            if (list.isEmpty()) {
                chunkCache.remove(chunkPos);
            }
        }
    }

    public static void addMultiblock(AbstractNCMultiblock multiblock, boolean force) {
        if (multiblocks.containsKey(multiblock.getId()) && force) {
            multiblocks.remove(multiblock.getId());
        }
        addMultiblock(multiblock);
    }

    public static void trackBlockChange(BlockPos pos) {
        if (pos == null || multiblocks.isEmpty()) {
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

    public static void tick() {
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


    public static void removeMultiblock(AbstractNCMultiblock multiblock) {
        multiblocks.remove(multiblock.getId());
        removeFromChunkCache(multiblock.getId());
    }


    public static boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
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
}
