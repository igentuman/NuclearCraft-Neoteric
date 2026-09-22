package igentuman.nc.multiblock.validation;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.geometry.LinearTubeFootprint;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StructureRecordBuilder {

    private final StructureRecord source;
    private final EnumMap<StructureRole, List<BlockPos>> roles = new EnumMap<>(StructureRole.class);
    private final Aggregates aggregates = new Aggregates();

    public StructureRecordBuilder(StructureRecord source) {
        if (source == null) throw new IllegalArgumentException("source record is required");
        this.source = source;
        List<BlockPos> controllers = source.roles().get(StructureRole.CONTROLLER);
        if (controllers != null && !controllers.isEmpty()) {
            roles.put(StructureRole.CONTROLLER, new ArrayList<>(controllers));
        }
    }

    public StructureRecordBuilder role(StructureRole role, BlockPos position) {
        List<BlockPos> positions = roles.computeIfAbsent(role, ignored -> new ArrayList<>());
        BlockPos immutable = position.immutable();
        if (!positions.contains(immutable)) positions.add(immutable);
        return this;
    }

    public Aggregates aggregates() {
        return aggregates;
    }

    public StructureRecord build(Map<Long, Long> sectionRevisions) {
        Map<StructureRole, List<BlockPos>> immutableRoles = new EnumMap<>(StructureRole.class);
        roles.forEach((role, positions) -> immutableRoles.put(role, List.copyOf(positions)));
        StructureFootprint footprint = withRoles(source.footprint(), immutableRoles);
        return new StructureRecord(source.schemaVersion(), source.id(), source.machineId(), source.dimension(),
                source.controllerPos(), source.orientation(), footprint, footprint.ownedSections(),
                footprint.watchedSections(), source.topologyRevision(), source.configRevision(), sectionRevisions,
                immutableRoles, aggregates.build(), source.state(), null);
    }

    private static StructureFootprint withRoles(StructureFootprint footprint,
                                                 Map<StructureRole, List<BlockPos>> roles) {
        if (footprint instanceof BoxFootprint box) {
            return new BoxFootprint(box.transform(), box.width(), box.height(), box.depth(), box.ownedSections(),
                    box.watchedSections(), roles);
        }
        if (footprint instanceof LinearTubeFootprint linear) {
            return new LinearTubeFootprint(linear.transform(), linear.length(), linear.crossSection(),
                    linear.ownedSections(), linear.watchedSections(), roles);
        }
        SquareRingFootprint ring = (SquareRingFootprint) footprint;
        return new SquareRingFootprint(ring.transform(), ring.outerSide(), ring.height(), ring.wallOffset(),
                ring.ownedSections(), ring.watchedSections(), roles);
    }

    public static final class Aggregates {
        private int beamLength;
        private long voltage;
        private double dipoleField;
        private double quadrupoleField;
        private long energyPerTick;
        private long heatPerTick;
        private long heatCapacity;
        private long coolingPerTick;
        private long maximumTemperatureK;
        private double efficiency;
        private final Map<ResourceLocation, Integer> detectorCounts = new LinkedHashMap<>();
        private int portChannels;

        public Aggregates beamLength(int value) { beamLength = value; return this; }
        public Aggregates addVoltage(long value) { voltage = Math.addExact(voltage, value); return this; }
        public Aggregates addDipoleField(double value) { dipoleField += value; return this; }
        public Aggregates addQuadrupoleField(double value) { quadrupoleField += value; return this; }
        public Aggregates addEnergyPerTick(long value) { energyPerTick = Math.addExact(energyPerTick, value); return this; }
        public Aggregates addHeatPerTick(long value) { heatPerTick = Math.addExact(heatPerTick, value); return this; }
        public Aggregates heatCapacity(long value) { heatCapacity = value; return this; }
        public Aggregates addCoolingPerTick(long value) { coolingPerTick = Math.addExact(coolingPerTick, value); return this; }
        public Aggregates includeMaximumTemperatureK(long value) {
            if (value > 0) maximumTemperatureK = maximumTemperatureK == 0 ? value : Math.min(maximumTemperatureK, value);
            return this;
        }
        public Aggregates efficiency(double value) { efficiency = value; return this; }
        public Aggregates addEfficiency(double value) { efficiency += value; return this; }
        public Aggregates detector(ResourceLocation id) { detectorCounts.merge(id, 1, Math::addExact); return this; }
        public Aggregates portChannels(int value) { portChannels = value; return this; }

        private StructureRecord.StructureAggregates build() {
            return new StructureRecord.StructureAggregates(beamLength, voltage, dipoleField, quadrupoleField,
                    energyPerTick, heatPerTick, heatCapacity, coolingPerTick, maximumTemperatureK, efficiency,
                    detectorCounts, portChannels);
        }
    }
}
