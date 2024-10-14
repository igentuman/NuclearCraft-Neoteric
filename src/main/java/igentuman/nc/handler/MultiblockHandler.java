package igentuman.nc.handler;

import igentuman.nc.multiblock.AbstractNCMultiblock;
import net.minecraft.core.BlockPos;

import java.util.HashMap;

public class MultiblockHandler {

    private static HashMap<String, AbstractNCMultiblock> multiblocks = new HashMap<>();

    public static void addMultiblock(AbstractNCMultiblock multiblock) {
        if(!multiblocks.containsKey(multiblock.getId())) {
            multiblocks.put(multiblock.getId(), multiblock);
        } else {
            multiblocks.putIfAbsent(multiblock.getId(), multiblock);
        }
    }

    public static void trackBlockChange(BlockPos pos) {
        for(AbstractNCMultiblock multiblock: multiblocks.values()) {
            if(multiblock == null) {
                continue;
            }
            if(multiblock.onBlockChange(pos)) {
                break;
            }
        }
    }

    public static void tick() {
        for(String id: multiblocks.keySet()) {
            AbstractNCMultiblock multiblock = multiblocks.get(id);
            if(multiblock == null) {
                multiblocks.remove(id);
            }
        }
    }

    public static void removeMultiblock(AbstractNCMultiblock multiblock) {
        multiblocks.remove(multiblock.getId());
    }
}
