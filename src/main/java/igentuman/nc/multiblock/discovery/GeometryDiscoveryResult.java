package igentuman.nc.multiblock.discovery;

import igentuman.nc.api.multiblock.StructureFootprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public record GeometryDiscoveryResult(
        Status status,
        String diagnosticKey,
        BlockPos failingPosition,
        SectionPos waitingSection,
        StructureFootprint footprint,
        long completedReads
) {
    public GeometryDiscoveryResult {
        if (status == null || diagnosticKey == null) {
            throw new IllegalArgumentException("Discovery status and diagnostic are required");
        }
        if (completedReads < 0) throw new IllegalArgumentException("completedReads must be nonnegative");
        if (failingPosition != null) failingPosition = failingPosition.immutable();
        if (status == Status.VALID && footprint == null) {
            throw new IllegalArgumentException("Successful discovery requires a footprint");
        }
    }

    public boolean terminal() {
        return status == Status.VALID || status == Status.INVALID || status == Status.CANCELLED;
    }

    public enum Status {
        IN_PROGRESS,
        WAITING_FOR_CHUNK,
        VALID,
        INVALID,
        CANCELLED
    }
}
