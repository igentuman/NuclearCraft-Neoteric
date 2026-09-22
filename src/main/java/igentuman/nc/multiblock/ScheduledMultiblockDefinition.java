package igentuman.nc.multiblock;

import igentuman.nc.multiblock.discovery.GeometryDiscoveryJobFactory;
import igentuman.nc.multiblock.validation.ValidationJobFactory;
import net.minecraft.resources.ResourceLocation;

public record ScheduledMultiblockDefinition(
        ResourceLocation machineId,
        GeometryDiscoveryJobFactory discoveryFactory,
        ValidationJobFactory validationFactory
) {
    public ScheduledMultiblockDefinition {
        if (machineId == null || discoveryFactory == null || validationFactory == null) {
            throw new IllegalArgumentException("Scheduled multiblock definition fields are required");
        }
    }
}
