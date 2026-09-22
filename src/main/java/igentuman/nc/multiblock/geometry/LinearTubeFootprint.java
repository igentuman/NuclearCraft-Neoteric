package igentuman.nc.multiblock.geometry;

import igentuman.nc.api.multiblock.StructureFootprint;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record LinearTubeFootprint(
        StructureTransform transform,
        int length,
        int crossSection,
        Set<Long> ownedSections,
        Set<Long> watchedSections,
        Map<StructureRole, List<BlockPos>> roles
) implements StructureFootprint {

    public LinearTubeFootprint {
        FootprintSupport.requirePositive("length", length);
        FootprintSupport.requirePositive("crossSection", crossSection);
        if (transform == null) throw new IllegalArgumentException("transform is required");
        roles = roles == null ? Map.of() : roles;
        roles = FootprintSupport.immutableRoles(roles, pos -> contains(transform, length, crossSection, pos));
        ownedSections = FootprintSupport.validatedOwnedSections(ownedSections,
                FootprintSupport.deriveBoxSections(transform, crossSection, crossSection, length));
        watchedSections = FootprintSupport.immutableSections(watchedSections);
    }

    public LinearTubeFootprint(StructureTransform transform, int length, int crossSection,
                               Map<StructureRole, List<BlockPos>> roles) {
        this(transform, length, crossSection, Set.of(), Set.of(), roles);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return contains(transform, length, crossSection, pos);
    }

    @Override
    public long cellCount() {
        return Math.multiplyExact((long) length, Math.multiplyExact((long) crossSection, crossSection));
    }

    @Override
    public Cursor cursor() {
        return cursor(transform, length, crossSection, roles);
    }

    @Override
    public List<BlockPos> positions(StructureRole role) {
        return FootprintSupport.positions(roles, role);
    }

    private static Cursor cursor(StructureTransform transform, int length, int crossSection,
                                 Map<StructureRole, List<BlockPos>> roles) {
        return new FootprintSupport.AbstractCursor(transform, roles,
                Math.multiplyExact((long) length, Math.multiplyExact((long) crossSection, crossSection))) {
            private int right;
            private int up;
            private int forward;

            @Override
            public Cell next() {
                Cell result = cell(right, up, forward);
                if (++right == crossSection) {
                    right = 0;
                    if (++up == crossSection) {
                        up = 0;
                        forward++;
                    }
                }
                return result;
            }
        };
    }

    private static boolean contains(StructureTransform transform, int length, int crossSection, BlockPos pos) {
        StructureTransform.LocalPosition local = transform.toLocal(pos);
        return local.forward() >= 0 && local.forward() < length
                && local.right() >= 0 && local.right() < crossSection
                && local.up() >= 0 && local.up() < crossSection;
    }
}
