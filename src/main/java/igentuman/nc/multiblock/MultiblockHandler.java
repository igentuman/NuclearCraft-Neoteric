package igentuman.nc.multiblock;

import igentuman.nc.block.fission.FissionFuelCellBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultiblockHandler {

    private static HashMap<String, AbstractNCMultiblock> multiblocks = new HashMap<>();

    public static void addMultiblock(AbstractNCMultiblock multiblock) {
        if(!multiblocks.containsKey(multiblock.getId())) {
            multiblocks.put(multiblock.getId(), multiblock);
        } else {
            multiblocks.putIfAbsent(multiblock.getId(), multiblock);
        }
    }

    public static void addMultiblock(AbstractNCMultiblock multiblock, boolean force) {
        if(multiblocks.containsKey(multiblock.getId()) && force) {
            multiblocks.remove(multiblock.getId());
        }
        addMultiblock(multiblock);
    }

    public static void trackBlockChange(BlockPos pos) {
        List<AbstractNCMultiblock> tmp = new ArrayList<>(multiblocks.values());
        for(AbstractNCMultiblock multiblock: tmp) {
            if(multiblock == null) {
                continue;
            }
            if(multiblock.onBlockChange(pos)) {
                break;
            }
        }
    }

    private static List<String> toRemove = new ArrayList<>();

    public static void tick() {
        Set<String> tmp = multiblocks.keySet();
        for(String id: tmp) {
            AbstractNCMultiblock multiblock = multiblocks.get(id);
            if(multiblock == null || multiblock.controller().controllerBE() == null) {
                toRemove.add(id);
                continue;
            }
            if(multiblock.isLoaded()) {
                if(multiblock.controller().controllerBE().isRemoved()) {
                    toRemove.add(id);
                    continue;
                }
                multiblock.tick();
            }
        }
        if(!toRemove.isEmpty()) {
            for(String id: toRemove) {
                multiblocks.remove(id);
            }
            toRemove.clear();
        }
    }

    public static void removeMultiblock(AbstractNCMultiblock multiblock) {
        multiblocks.remove(multiblock.getId());
    }


    public static boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
        for(AbstractNCMultiblock multiblock: multiblocks.values()) {
            if(multiblock == null) {
                continue;
            }
            if(multiblock.checkAttachmentToBlock(toCheck, level, pos, dir)) {
                return true;
            }
        }
        return false;
    }
}
