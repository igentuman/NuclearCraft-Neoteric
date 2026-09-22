package igentuman.nc.multiblock.validation;

import igentuman.nc.api.multiblock.StructureFootprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public abstract class FootprintValidationJob implements ValidationJob {

    protected final UUID structureId;
    protected final StructureFootprint footprint;
    private final StructureFootprint.Cursor cursor;
    private StructureFootprint.Cell pendingCell;
    private long completedCells;
    private boolean finished;
    private Long structuralRevision;
    private long configRevision;

    protected FootprintValidationJob(UUID structureId, StructureFootprint footprint) {
        if (structureId == null || footprint == null) throw new IllegalArgumentException("Validation job fields are required");
        this.structureId = structureId;
        this.footprint = footprint;
        this.cursor = footprint.cursor();
    }

    @Override
    public final UUID structureId() {
        return structureId;
    }

    @Override
    public final ValidationResult advance(LoadedStructureReader reader, ValidationBudget budget) {
        if (finished) return result(ValidationStatus.CANCELLED, "multiblock.validation.job_finished", null, null);
        if (structuralRevision == null) {
            structuralRevision = reader.structuralRevision(structureId);
            configRevision = reader.configRevision();
        } else if (structuralRevision != reader.structuralRevision(structureId)
                || configRevision != reader.configRevision()) {
            finished = true;
            return result(ValidationStatus.STALE, "multiblock.validation.structure_changed", null, null);
        }

        long started = System.nanoTime();
        int reads = 0;
        int blockEntityReads = 0;
        while (reads < budget.maximumBlockReads() && System.nanoTime() - started < budget.maximumNanos()) {
            if (pendingCell == null) {
                if (!cursor.hasNext()) {
                    finished = true;
                    return completedResult(Map.of());
                }
                pendingCell = cursor.next();
            }

            SectionPos section = SectionPos.of(pendingCell.pos());
            if (!reader.isLoaded(section)) {
                return result(ValidationStatus.WAITING_FOR_CHUNK, "multiblock.validation.waiting_for_chunk",
                        null, section);
            }

            BlockState state = reader.blockState(pendingCell.pos());
            reader.validationObserved(structureId, pendingCell.pos(), state);
            reads++;
            BlockEntity blockEntity = null;
            if (requiresBlockEntity(pendingCell, state)) {
                if (blockEntityReads >= budget.maximumBlockEntityReads()) {
                    return result(ValidationStatus.IN_PROGRESS, "multiblock.validation.in_progress", null, null);
                }
                blockEntity = reader.blockEntity(pendingCell.pos());
                blockEntityReads++;
            }
            ValidationFailure failure = validateLoadedCell(pendingCell, state, blockEntity);
            if (failure != null) {
                finished = true;
                return new ValidationResult(ValidationStatus.INVALID, failure.diagnosticKey(), pendingCell.pos(),
                        failure.expected(), failure.actual(), completedCells, footprint.cellCount(), null, null);
            }
            pendingCell = null;
            completedCells++;
            if (!cursor.hasNext()) {
                finished = true;
                return completedResult(Map.of());
            }
        }
        return result(ValidationStatus.IN_PROGRESS, "multiblock.validation.in_progress", null, null);
    }

    protected boolean requiresBlockEntity(StructureFootprint.Cell cell, BlockState state) {
        return false;
    }

    protected ValidationFailure validateLoadedCell(StructureFootprint.Cell cell, BlockState state,
                                                     @Nullable BlockEntity blockEntity) {
        return null;
    }

    protected ValidationResult completedResult(Map<Long, Long> sectionRevisions) {
        return result(ValidationStatus.VALID, "multiblock.validation.valid", null, null);
    }

    public final long completedCells() {
        return completedCells;
    }

    public final Map<Long, Long> scannedSectionRevisions() {
        return Map.of();
    }

    private ValidationResult result(ValidationStatus status, String key, BlockPos failingPosition,
                                    SectionPos waitingSection) {
        return new ValidationResult(status, key, failingPosition, null, null, completedCells,
                footprint.cellCount(), waitingSection, null);
    }

    public record ValidationFailure(String diagnosticKey, ResourceLocation expected, ResourceLocation actual) {
    }
}
