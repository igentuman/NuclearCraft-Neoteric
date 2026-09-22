package igentuman.nc.multiblock.validation;

import java.util.UUID;

public interface ValidationJob {

    UUID structureId();

    ValidationResult advance(LoadedStructureReader reader, ValidationBudget budget);
}
