package igentuman.nc.multiblock.geometry;

import igentuman.nc.api.multiblock.StructureFootprint;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SquareRingFootprint(
        StructureTransform transform,
        int outerSide,
        int height,
        int wallOffset,
        Set<Long> ownedSections,
        Set<Long> watchedSections,
        Map<StructureRole, List<BlockPos>> roles
) implements StructureFootprint {

    public SquareRingFootprint {
        FootprintSupport.requirePositive("outerSide", outerSide);
        FootprintSupport.requirePositive("height", height);
        FootprintSupport.requirePositive("wallOffset", wallOffset);
        if ((long) outerSide <= (long) wallOffset * 2) {
            throw new IllegalArgumentException("Ring must have a hollow center");
        }
        if (transform == null) throw new IllegalArgumentException("transform is required");
        roles = roles == null ? Map.of() : roles;
        roles = FootprintSupport.immutableRoles(roles,
                pos -> contains(transform, outerSide, height, wallOffset, pos));
        ownedSections = FootprintSupport.validatedOwnedSections(ownedSections,
                deriveOwnedSections(transform, outerSide, height, wallOffset));
        watchedSections = FootprintSupport.immutableSections(watchedSections);
    }

    public SquareRingFootprint(StructureTransform transform, int outerSide, int height, int wallOffset,
                               Map<StructureRole, List<BlockPos>> roles) {
        this(transform, outerSide, height, wallOffset, Set.of(), Set.of(), roles);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return contains(transform, outerSide, height, wallOffset, pos);
    }

    private static boolean contains(StructureTransform transform, int outerSide, int height, int wallOffset,
                                    BlockPos pos) {
        StructureTransform.LocalPosition local = transform.toLocal(pos);
        if (local.up() < 0 || local.up() >= height || local.right() < 0 || local.right() >= outerSide
                || local.forward() < 0 || local.forward() >= outerSide) return false;
        return local.right() < wallOffset || local.right() >= outerSide - wallOffset
                || local.forward() < wallOffset || local.forward() >= outerSide - wallOffset;
    }

    @Override
    public long cellCount() {
        long area = Math.subtractExact(Math.multiplyExact((long) outerSide, outerSide),
                Math.multiplyExact((long) (outerSide - 2 * wallOffset), outerSide - 2 * wallOffset));
        return Math.multiplyExact(area, height);
    }

    @Override
    public Cursor cursor() {
        return cursor(transform, outerSide, height, wallOffset, roles);
    }

    @Override
    public List<BlockPos> positions(StructureRole role) {
        return FootprintSupport.positions(roles, role);
    }

    private static Cursor cursor(StructureTransform transform, int outerSide, int height, int wallOffset,
                                 Map<StructureRole, List<BlockPos>> roles) {
        long innerSide = outerSide - 2L * wallOffset;
        long count = Math.multiplyExact(
                Math.subtractExact(Math.multiplyExact((long) outerSide, outerSide), innerSide * innerSide), height);
        return new FootprintSupport.AbstractCursor(transform, roles, count) {
            private int right;
            private int up;
            private int forward;

            @Override
            public Cell next() {
                Cell result = cell(right, up, forward);
                advancePosition();
                return result;
            }

            private void advancePosition() {
                right++;
                if (right == outerSide) {
                    right = 0;
                    forward++;
                    if (forward == outerSide) {
                        forward = 0;
                        up++;
                    }
                }
                if (up < height && forward >= wallOffset && forward < outerSide - wallOffset
                        && right == wallOffset) {
                    right = outerSide - wallOffset;
                }
            }
        };
    }

    private static Set<Long> deriveOwnedSections(StructureTransform transform, int outerSide, int height,
                                                 int wallOffset) {
        Set<Long> sections = new HashSet<>();
        sections.addAll(FootprintSupport.deriveBoxSections(transform, outerSide, height, wallOffset));
        sections.addAll(FootprintSupport.deriveBoxSections(
                shifted(transform, 0, outerSide - wallOffset), outerSide, height, wallOffset));
        int innerSide = outerSide - 2 * wallOffset;
        sections.addAll(FootprintSupport.deriveBoxSections(
                shifted(transform, 0, wallOffset), wallOffset, height, innerSide));
        sections.addAll(FootprintSupport.deriveBoxSections(
                shifted(transform, outerSide - wallOffset, wallOffset), wallOffset, height, innerSide));
        return sections;
    }

    private static StructureTransform shifted(StructureTransform transform, int right, int forward) {
        return new StructureTransform(transform.toWorld(right, 0, forward), transform.forward(), transform.right(),
                transform.up());
    }

}
