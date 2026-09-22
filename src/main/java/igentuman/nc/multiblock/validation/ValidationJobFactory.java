package igentuman.nc.multiblock.validation;

import igentuman.nc.multiblock.StructureRecord;

import java.util.UUID;

public interface ValidationJobFactory {

    ValidationJob create(UUID structureId, StructureRecord record);
}
