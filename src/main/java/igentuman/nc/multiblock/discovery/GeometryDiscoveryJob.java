package igentuman.nc.multiblock.discovery;

import igentuman.nc.multiblock.validation.LoadedStructureReader;
import igentuman.nc.multiblock.validation.ValidationBudget;

import java.util.UUID;

public interface GeometryDiscoveryJob {

    UUID structureId();

    GeometryDiscoveryResult advance(LoadedStructureReader reader, ValidationBudget budget);
}
