package igentuman.nc.multiblock;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record StructureRecord(
        int schemaVersion,
        UUID id,
        ResourceLocation machineId,
        ResourceKey<Level> dimension,
        BlockPos controllerPos,
        Direction orientation,
        StructureFootprint footprint,
        Set<Long> ownedSections,
        Set<Long> watchedSections,
        long topologyRevision,
        long configRevision,
        Map<Long, Long> sectionRevisions,
        Map<StructureRole, List<BlockPos>> roles,
        StructureAggregates aggregates,
        StructureLifecycleState state,
        StructureDiagnostic diagnostic
) {

    public StructureRecord {
        if (schemaVersion <= 0) throw new IllegalArgumentException("schemaVersion must be positive");
        if (id == null || machineId == null || dimension == null || controllerPos == null || orientation == null
                || footprint == null || aggregates == null || state == null) {
            throw new IllegalArgumentException("Structure record fields may not be null");
        }
        controllerPos = controllerPos.immutable();
        ownedSections = Set.copyOf(footprint.ownedSections());
        watchedSections = Set.copyOf(footprint.watchedSections());
        sectionRevisions = sectionRevisions == null ? Map.of() : Map.copyOf(sectionRevisions);
        roles = immutableRoles(roles);
    }

    public StructureRecord withState(StructureLifecycleState newState, StructureDiagnostic newDiagnostic) {
        return new StructureRecord(schemaVersion, id, machineId, dimension, controllerPos, orientation, footprint,
                ownedSections, watchedSections, topologyRevision, configRevision, sectionRevisions, roles,
                aggregates, newState, newDiagnostic);
    }

    public StructureRecord withRevisions(long newTopologyRevision, long newConfigRevision,
                                         Map<Long, Long> newSectionRevisions) {
        return new StructureRecord(schemaVersion, id, machineId, dimension, controllerPos, orientation, footprint,
                ownedSections, watchedSections, newTopologyRevision, newConfigRevision, newSectionRevisions, roles,
                aggregates, state, diagnostic);
    }

    private static Map<StructureRole, List<BlockPos>> immutableRoles(Map<StructureRole, List<BlockPos>> source) {
        if (source == null || source.isEmpty()) return Map.of();
        java.util.EnumMap<StructureRole, List<BlockPos>> copy = new java.util.EnumMap<>(StructureRole.class);
        source.forEach((role, positions) -> {
            if (role == null || positions == null) throw new IllegalArgumentException("Null structure role");
            copy.put(role, positions.stream().map(pos -> {
                if (pos == null) throw new IllegalArgumentException("Null structure role position");
                return pos.immutable();
            }).toList());
        });
        return java.util.Collections.unmodifiableMap(copy);
    }

    public record StructureAggregates(
            int beamLength,
            long voltage,
            double dipoleField,
            double quadrupoleField,
            long energyPerTick,
            long heatPerTick,
            long heatCapacity,
            long coolingPerTick,
            long maximumTemperatureK,
            double efficiency,
            Map<ResourceLocation, Integer> detectorCounts,
            int portChannels
    ) {
        public StructureAggregates {
            detectorCounts = detectorCounts == null ? Map.of() : Map.copyOf(detectorCounts);
        }
    }

    public record StructureDiagnostic(
            String translationKey,
            BlockPos position,
            ResourceLocation expected,
            ResourceLocation actual
    ) {
        public StructureDiagnostic {
            if (translationKey == null) translationKey = "";
            if (position != null) position = position.immutable();
        }
    }
}
