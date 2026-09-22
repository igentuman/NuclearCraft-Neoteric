package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record StructureChangeTracker(
        ResourceKey<Level> dimension,
        BlockPos position,
        long sectionRevision,
        Cause cause
) {

    public enum Cause {
        PLACE,
        BREAK,
        EXPLOSION,
        PISTON,
        BLOCK_STATE,
        CHUNK_AUDIT,
        EXPLICIT
    }
}
