package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;

public record StructureApplicationJob(
        UUID structureId,
        Operation operation,
        List<BlockPos> portPositions,
        int cursor,
        long generation
) {

    public static final int MAX_PORT_POSITIONS = 256;

    public StructureApplicationJob(UUID id, Operation operation, List<BlockPos> positions, int cursor) {
        this(id, operation, positions, cursor, 0);
    }

    public StructureApplicationJob {
        if (structureId == null || operation == null || portPositions == null) {
            throw new IllegalArgumentException("Application job fields may not be null");
        }
        if (portPositions.size() > MAX_PORT_POSITIONS) {
            throw new IllegalArgumentException("Too many externally addressable ports: " + portPositions.size());
        }
        if (cursor < 0 || cursor > portPositions.size()) throw new IllegalArgumentException("Invalid cursor");
        portPositions = portPositions.stream().map(BlockPos::immutable).toList();
    }

    public boolean isComplete() {
        return cursor >= portPositions.size();
    }

    public BlockPos currentPosition() {
        if (isComplete()) throw new java.util.NoSuchElementException();
        return portPositions.get(cursor);
    }

    public StructureApplicationJob advance() {
        if (isComplete()) return this;
        return new StructureApplicationJob(structureId, operation, portPositions, cursor + 1, generation);
    }

    public enum Operation {
        FORM,
        BREAK
    }
}
