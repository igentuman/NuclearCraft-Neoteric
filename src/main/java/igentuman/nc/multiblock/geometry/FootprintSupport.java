package igentuman.nc.multiblock.geometry;

import igentuman.nc.api.multiblock.StructureFootprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

final class FootprintSupport {

    private FootprintSupport() {
    }

    static void requirePositive(String name, int value) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive");
    }

    static Map<StructureRole, List<BlockPos>> immutableRoles(
            Map<StructureRole, List<BlockPos>> roles, Predicate<BlockPos> contains) {
        if (roles == null || roles.isEmpty()) return Map.of();
        EnumMap<StructureRole, List<BlockPos>> copy = new EnumMap<>(StructureRole.class);
        roles.forEach((role, positions) -> {
            if (role == null || positions == null) throw new IllegalArgumentException("Null footprint role");
            List<BlockPos> immutablePositions = positions.stream().map(pos -> {
                if (pos == null || !contains.test(pos)) {
                    throw new IllegalArgumentException("Role position is outside the footprint: " + pos);
                }
                return pos.immutable();
            }).toList();
            copy.put(role, immutablePositions);
        });
        return Collections.unmodifiableMap(copy);
    }

    static Set<Long> immutableSections(Set<Long> sections) {
        return sections == null || sections.isEmpty() ? Set.of() : Set.copyOf(sections);
    }

    static Set<Long> validatedOwnedSections(Set<Long> supplied, StructureFootprint.Cursor cursor) {
        return validatedOwnedSections(supplied, deriveSections(cursor));
    }

    static Set<Long> validatedOwnedSections(Set<Long> supplied, Set<Long> derived) {
        if (supplied != null && !supplied.isEmpty() && !derived.equals(supplied)) {
            throw new IllegalArgumentException("Stored footprint sections do not match its geometry");
        }
        return Set.copyOf(derived);
    }

    static Set<Long> deriveBoxSections(StructureTransform transform, int width, int height, int depth) {
        BlockPos first = transform.origin();
        BlockPos last = transform.toWorld(width - 1, height - 1, depth - 1);
        int minSectionX = SectionPos.blockToSectionCoord(Math.min(first.getX(), last.getX()));
        int maxSectionX = SectionPos.blockToSectionCoord(Math.max(first.getX(), last.getX()));
        int minSectionY = SectionPos.blockToSectionCoord(Math.min(first.getY(), last.getY()));
        int maxSectionY = SectionPos.blockToSectionCoord(Math.max(first.getY(), last.getY()));
        int minSectionZ = SectionPos.blockToSectionCoord(Math.min(first.getZ(), last.getZ()));
        int maxSectionZ = SectionPos.blockToSectionCoord(Math.max(first.getZ(), last.getZ()));
        Set<Long> sections = new HashSet<>();
        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    sections.add(SectionPos.asLong(x, y, z));
                }
            }
        }
        return sections;
    }

    static Set<Long> deriveSections(StructureFootprint.Cursor cursor) {
        Set<Long> sections = new HashSet<>();
        while (cursor.hasNext()) {
            BlockPos pos = cursor.next().pos();
            sections.add(SectionPos.asLong(
                    SectionPos.blockToSectionCoord(pos.getX()),
                    SectionPos.blockToSectionCoord(pos.getY()),
                    SectionPos.blockToSectionCoord(pos.getZ())));
        }
        return Set.copyOf(sections);
    }

    static List<BlockPos> positions(Map<StructureRole, List<BlockPos>> roles, StructureRole role) {
        if (role == null) return List.of();
        return roles.getOrDefault(role, List.of());
    }

    static StructureRole roleAt(Map<StructureRole, List<BlockPos>> roles, BlockPos pos) {
        for (Map.Entry<StructureRole, List<BlockPos>> entry : roles.entrySet()) {
            if (entry.getValue().contains(pos)) return entry.getKey();
        }
        return null;
    }

    abstract static class AbstractCursor implements StructureFootprint.Cursor {

        final StructureTransform transform;
        final Map<StructureRole, List<BlockPos>> roles;
        long remaining;

        AbstractCursor(StructureTransform transform, Map<StructureRole, List<BlockPos>> roles, long count) {
            this.transform = transform;
            this.roles = roles;
            this.remaining = count;
        }

        @Override
        public final boolean hasNext() {
            return remaining > 0;
        }

        final StructureFootprint.Cell cell(int right, int up, int forward) {
            if (!hasNext()) throw new NoSuchElementException();
            remaining--;
            BlockPos pos = transform.toWorld(right, up, forward);
            return new StructureFootprint.Cell(pos, StructureFootprint.CellKind.OWNED, roleAt(roles, pos));
        }
    }
}
