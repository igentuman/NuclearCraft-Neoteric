package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.validation.FootprintValidationJob;
import igentuman.nc.multiblock.validation.StructureRecordBuilder;

import java.util.UUID;

public abstract class ParticleChamberValidationJob extends FootprintValidationJob {

    protected final StructureRecord source;
    protected final StructureRecordBuilder recordBuilder;

    protected ParticleChamberValidationJob(UUID structureId, StructureRecord source) {
        super(structureId, source.footprint());
        this.source = source;
        this.recordBuilder = new StructureRecordBuilder(source);
    }
}
