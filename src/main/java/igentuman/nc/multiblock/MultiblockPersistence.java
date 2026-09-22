package igentuman.nc.multiblock;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.geometry.LinearTubeFootprint;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.geometry.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MultiblockPersistence {

    public static final int SCHEMA_VERSION = 1;

    private MultiblockPersistence() {
    }

    public static CompoundTag saveRecord(StructureRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", record.schemaVersion());
        tag.putUUID("id", record.id());
        tag.putString("machine", record.machineId().toString());
        tag.putString("dimension", record.dimension().location().toString());
        tag.putLong("controller", record.controllerPos().asLong());
        tag.putString("orientation", record.orientation().getName());
        tag.put("footprint", saveFootprint(record.footprint(), true));
        tag.putLong("topology_revision", record.topologyRevision());
        tag.putLong("config_revision", record.configRevision());
        tag.put("section_revisions", saveLongMap(record.sectionRevisions()));
        tag.put("roles", saveRoles(record.roles()));
        tag.put("aggregates", saveAggregates(record.aggregates()));
        tag.putString("state", record.state().name());
        if (record.diagnostic() != null) tag.put("diagnostic", saveDiagnostic(record.diagnostic()));
        return tag;
    }

    public static Optional<StructureRecord> loadRecord(CompoundTag tag) {
        try {
            int schema = tag.getInt("schema");
            if (schema != SCHEMA_VERSION || !tag.hasUUID("id")) return Optional.empty();
            ResourceLocation machine = requiredLocation(tag.getString("machine"));
            ResourceLocation dimensionId = requiredLocation(tag.getString("dimension"));
            Direction orientation = Direction.byName(tag.getString("orientation"));
            if (orientation == null) return Optional.empty();
            StructureFootprint footprint = loadFootprint(tag.getCompound("footprint"));
            StructureRecord record = new StructureRecord(schema, tag.getUUID("id"), machine,
                    ResourceKey.create(Registries.DIMENSION, dimensionId), BlockPos.of(tag.getLong("controller")),
                    orientation, footprint, footprint.ownedSections(), footprint.watchedSections(),
                    tag.getLong("topology_revision"), tag.getLong("config_revision"),
                    loadLongMap(tag.getList("section_revisions", Tag.TAG_COMPOUND)),
                    loadRoles(tag.getList("roles", Tag.TAG_COMPOUND)),
                    loadAggregates(tag.getCompound("aggregates")),
                    StructureLifecycleState.valueOf(tag.getString("state")),
                    tag.contains("diagnostic", Tag.TAG_COMPOUND)
                            ? loadDiagnostic(tag.getCompound("diagnostic")) : null);
            return Optional.of(record);
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return Optional.empty();
        }
    }

    public static CompoundTag saveClientGeometry(StructureFootprint footprint) {
        return saveFootprint(footprint, false);
    }

    public static CompoundTag saveFootprint(StructureFootprint footprint, boolean includeSections) {
        CompoundTag tag = new CompoundTag();
        saveTransform(tag, footprint);
        if (footprint instanceof BoxFootprint box) {
            tag.putString("type", "box");
            tag.putInt("width", box.width());
            tag.putInt("height", box.height());
            tag.putInt("depth", box.depth());
        } else if (footprint instanceof LinearTubeFootprint linear) {
            tag.putString("type", "linear_tube");
            tag.putInt("length", linear.length());
            tag.putInt("cross_section", linear.crossSection());
        } else if (footprint instanceof SquareRingFootprint ring) {
            tag.putString("type", "square_ring");
            tag.putInt("outer_side", ring.outerSide());
            tag.putInt("height", ring.height());
            tag.putInt("wall_offset", ring.wallOffset());
        } else {
            throw new IllegalArgumentException("Unsupported footprint type " + footprint.getClass().getName());
        }
        tag.put("roles", saveRoles(roleMap(footprint)));
        if (includeSections) {
            tag.putLongArray("owned_sections", footprint.ownedSections().stream().mapToLong(Long::longValue).toArray());
            tag.putLongArray("watched_sections", footprint.watchedSections().stream().mapToLong(Long::longValue).toArray());
        }
        return tag;
    }

    public static StructureFootprint loadFootprint(CompoundTag tag) {
        StructureTransform transform = loadTransform(tag);
        Set<Long> owned = toSet(tag.getLongArray("owned_sections"));
        Set<Long> watched = Set.of(); // Old discovery halos never participate in structural tracking.
        Map<StructureRole, List<BlockPos>> roles = loadRoles(tag.getList("roles", Tag.TAG_COMPOUND));
        return switch (tag.getString("type")) {
            case "box" -> new BoxFootprint(transform, tag.getInt("width"), tag.getInt("height"),
                    tag.getInt("depth"), owned, watched, roles);
            case "linear_tube" -> new LinearTubeFootprint(transform, tag.getInt("length"),
                    tag.getInt("cross_section"), owned, watched, roles);
            case "square_ring" -> new SquareRingFootprint(transform, tag.getInt("outer_side"),
                    tag.getInt("height"), tag.getInt("wall_offset"), owned, watched, roles);
            default -> throw new IllegalArgumentException("Unknown footprint type " + tag.getString("type"));
        };
    }

    private static void saveTransform(CompoundTag tag, StructureFootprint footprint) {
        StructureTransform transform = switch (footprint) {
            case BoxFootprint box -> box.transform();
            case LinearTubeFootprint linear -> linear.transform();
            case SquareRingFootprint ring -> ring.transform();
            default -> throw new IllegalArgumentException("Unsupported footprint type");
        };
        tag.putLong("origin", transform.origin().asLong());
        tag.putString("forward", transform.forward().getName());
        tag.putString("right", transform.right().getName());
        tag.putString("up", transform.up().getName());
    }

    private static StructureTransform loadTransform(CompoundTag tag) {
        Direction forward = Direction.byName(tag.getString("forward"));
        Direction right = Direction.byName(tag.getString("right"));
        Direction up = Direction.byName(tag.getString("up"));
        if (forward == null || right == null || up == null) throw new IllegalArgumentException("Invalid transform");
        return new StructureTransform(BlockPos.of(tag.getLong("origin")), forward, right, up);
    }

    private static ListTag saveRoles(Map<StructureRole, List<BlockPos>> roles) {
        ListTag list = new ListTag();
        roles.forEach((role, positions) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("role", role.name());
            entry.putLongArray("positions", positions.stream().mapToLong(BlockPos::asLong).toArray());
            list.add(entry);
        });
        return list;
    }

    private static Map<StructureRole, List<BlockPos>> loadRoles(ListTag list) {
        EnumMap<StructureRole, List<BlockPos>> roles = new EnumMap<>(StructureRole.class);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            StructureRole role = StructureRole.valueOf(entry.getString("role"));
            roles.put(role, java.util.Arrays.stream(entry.getLongArray("positions")).mapToObj(BlockPos::of).toList());
        }
        return roles;
    }

    private static Map<StructureRole, List<BlockPos>> roleMap(StructureFootprint footprint) {
        EnumMap<StructureRole, List<BlockPos>> roles = new EnumMap<>(StructureRole.class);
        for (StructureRole role : StructureRole.values()) {
            List<BlockPos> positions = footprint.positions(role);
            if (!positions.isEmpty()) roles.put(role, positions);
        }
        return roles;
    }

    private static CompoundTag saveAggregates(StructureRecord.StructureAggregates value) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("beam_length", value.beamLength());
        tag.putLong("voltage", value.voltage());
        tag.putDouble("dipole_field", value.dipoleField());
        tag.putDouble("quadrupole_field", value.quadrupoleField());
        tag.putLong("energy_per_tick", value.energyPerTick());
        tag.putLong("heat_per_tick", value.heatPerTick());
        tag.putLong("heat_capacity", value.heatCapacity());
        tag.putLong("cooling_per_tick", value.coolingPerTick());
        tag.putLong("maximum_temperature_k", value.maximumTemperatureK());
        tag.putDouble("efficiency", value.efficiency());
        tag.putInt("port_channels", value.portChannels());
        ListTag detectors = new ListTag();
        value.detectorCounts().forEach((id, count) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", id.toString());
            entry.putInt("count", count);
            detectors.add(entry);
        });
        tag.put("detectors", detectors);
        return tag;
    }

    private static StructureRecord.StructureAggregates loadAggregates(CompoundTag tag) {
        Map<ResourceLocation, Integer> detectors = new HashMap<>();
        ListTag list = tag.getList("detectors", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            detectors.put(requiredLocation(entry.getString("id")), entry.getInt("count"));
        }
        return new StructureRecord.StructureAggregates(tag.getInt("beam_length"), tag.getLong("voltage"),
                tag.getDouble("dipole_field"), tag.getDouble("quadrupole_field"), tag.getLong("energy_per_tick"),
                tag.getLong("heat_per_tick"), tag.getLong("heat_capacity"), tag.getLong("cooling_per_tick"),
                tag.getLong("maximum_temperature_k"), tag.getDouble("efficiency"), detectors,
                tag.getInt("port_channels"));
    }

    private static CompoundTag saveDiagnostic(StructureRecord.StructureDiagnostic value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", value.translationKey());
        if (value.position() != null) tag.putLong("position", value.position().asLong());
        if (value.expected() != null) tag.putString("expected", value.expected().toString());
        if (value.actual() != null) tag.putString("actual", value.actual().toString());
        return tag;
    }

    private static StructureRecord.StructureDiagnostic loadDiagnostic(CompoundTag tag) {
        return new StructureRecord.StructureDiagnostic(tag.getString("key"),
                tag.contains("position") ? BlockPos.of(tag.getLong("position")) : null,
                optionalLocation(tag.getString("expected")), optionalLocation(tag.getString("actual")));
    }

    private static ListTag saveLongMap(Map<Long, Long> values) {
        ListTag list = new ListTag();
        values.forEach((key, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("section", key);
            entry.putLong("revision", value);
            list.add(entry);
        });
        return list;
    }

    private static Map<Long, Long> loadLongMap(ListTag list) {
        Map<Long, Long> values = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            values.put(entry.getLong("section"), entry.getLong("revision"));
        }
        return values;
    }

    private static Set<Long> toSet(long[] values) {
        java.util.HashSet<Long> result = new java.util.HashSet<>();
        for (long value : values) result.add(value);
        return Set.copyOf(result);
    }

    private static ResourceLocation requiredLocation(String value) {
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) throw new IllegalArgumentException("Invalid resource location " + value);
        return location;
    }

    private static ResourceLocation optionalLocation(String value) {
        return value == null || value.isEmpty() ? null : requiredLocation(value);
    }
}
