package igentuman.nc.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class StructureObservations {
    private final Long2ObjectOpenHashMap<int[]> sections = new Long2ObjectOpenHashMap<>();

    public void seed(BlockPos pos, BlockState state) {
        int[] cells = section(pos);
        int index = index(pos);
        if (cells[index] == 0) cells[index] = signature(state);
    }

    public boolean observe(BlockPos pos, BlockState state) {
        int[] cells = section(pos);
        int index = index(pos);
        int previous = cells[index];
        int current = signature(state);
        cells[index] = current;
        return previous != 0 && previous != current;
    }

    private int[] section(BlockPos pos) {
        return sections.computeIfAbsent(StructureSectionIndex.sectionKey(pos), ignored -> new int[4096]);
    }

    private static int index(BlockPos pos) {
        return (pos.getY() & 15) << 8 | (pos.getZ() & 15) << 4 | pos.getX() & 15;
    }

    private static int signature(BlockState state) {
        return Block.getId(StructuralBlockState.signature(state)) + 1;
    }
}
