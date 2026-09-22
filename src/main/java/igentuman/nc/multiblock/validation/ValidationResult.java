package igentuman.nc.multiblock.validation;

import igentuman.nc.multiblock.StructureRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;

public record ValidationResult(
        ValidationStatus status,
        String diagnosticKey,
        BlockPos failingPosition,
        ResourceLocation expected,
        ResourceLocation actual,
        long completedCells,
        long totalCells,
        SectionPos waitingSection,
        StructureRecord record
) {
    public ValidationResult {
        if (status == null || diagnosticKey == null) {
            throw new IllegalArgumentException("Validation result status and diagnostic are required");
        }
        if (completedCells < 0 || totalCells < 0 || completedCells > totalCells) {
            throw new IllegalArgumentException("Invalid validation progress");
        }
        if (failingPosition != null) failingPosition = failingPosition.immutable();
    }

    public boolean terminal() {
        return status == ValidationStatus.VALID || status == ValidationStatus.INVALID
                || status == ValidationStatus.STALE || status == ValidationStatus.CANCELLED;
    }
}
