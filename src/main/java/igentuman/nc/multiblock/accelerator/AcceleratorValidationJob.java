package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.validation.FootprintValidationJob;
import igentuman.nc.multiblock.validation.StructureRecordBuilder;

import java.util.UUID;

public abstract class AcceleratorValidationJob extends FootprintValidationJob {

    protected final StructureRecord source;
    protected final StructureRecordBuilder recordBuilder;

    protected AcceleratorValidationJob(UUID structureId, StructureRecord source) {
        super(structureId, source.footprint());
        this.source = source;
        this.recordBuilder = new StructureRecordBuilder(source);
    }
}
