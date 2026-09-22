package igentuman.nc.api.multiblock;

import igentuman.nc.multiblock.geometry.StructureRole;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Set;

public interface StructureFootprint {

    boolean contains(BlockPos pos);

    long cellCount();

    Set<Long> ownedSections();

    Set<Long> watchedSections();

    Cursor cursor();

    List<BlockPos> positions(StructureRole role);

    interface Cursor {

        boolean hasNext();

        Cell next();
    }

    record Cell(BlockPos pos, CellKind kind, StructureRole role) {
    }

    enum CellKind {
        VALIDATION,
        OWNED,
        WATCHED
    }
}
