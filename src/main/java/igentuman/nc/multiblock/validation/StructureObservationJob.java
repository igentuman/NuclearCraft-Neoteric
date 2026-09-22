package igentuman.nc.multiblock.validation;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.StructureObservations;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;

import java.util.UUID;

public final class StructureObservationJob implements ValidationJob {
    private final UUID id;
    private final StructureFootprint footprint;
    private final StructureFootprint.Cursor cursor;
    private final StructureObservations observations;
    private final LongSet changes = new LongOpenHashSet();
    private StructureFootprint.Cell pending;
    private long completed;

    public StructureObservationJob(UUID id, StructureFootprint footprint, StructureObservations observations) {
        this.id = id;
        this.footprint = footprint;
        this.cursor = footprint.cursor();
        this.observations = observations;
    }

    @Override
    public UUID structureId() { return id; }

    public LongSet changes() { return changes; }

    @Override
    public ValidationResult advance(LoadedStructureReader reader, ValidationBudget budget) {
        long started = System.nanoTime();
        int reads = 0;
        while (reads < budget.maximumBlockReads() && System.nanoTime() - started < budget.maximumNanos()) {
            if (pending == null) {
                if (!cursor.hasNext()) return result(ValidationStatus.VALID, null);
                pending = cursor.next();
            }
            SectionPos section = SectionPos.of(pending.pos());
            if (!reader.isLoaded(section)) return result(ValidationStatus.WAITING_FOR_CHUNK, section);
            if (observations.observe(pending.pos(), reader.blockState(pending.pos()))) changes.add(pending.pos().asLong());
            reads++;
            completed++;
            pending = null;
        }
        return result(cursor.hasNext() ? ValidationStatus.IN_PROGRESS : ValidationStatus.VALID, null);
    }

    private ValidationResult result(ValidationStatus status, SectionPos section) {
        return new ValidationResult(status, "multiblock.observation", null, null, null,
                completed, footprint.cellCount(), section, null);
    }
}
