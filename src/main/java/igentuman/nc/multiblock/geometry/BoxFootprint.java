package igentuman.nc.multiblock.geometry;

import igentuman.nc.api.multiblock.StructureFootprint;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record BoxFootprint(
        StructureTransform transform,
        int width,
        int height,
        int depth,
        Set<Long> ownedSections,
        Set<Long> watchedSections,
        Map<StructureRole, List<BlockPos>> roles
) implements StructureFootprint {

    public BoxFootprint {
        FootprintSupport.requirePositive("width", width);
        FootprintSupport.requirePositive("height", height);
        FootprintSupport.requirePositive("depth", depth);
        if (transform == null) throw new IllegalArgumentException("transform is required");
        roles = roles == null ? Map.of() : roles;
        Map<StructureRole, List<BlockPos>> roleCopy = roles;
        roles = FootprintSupport.immutableRoles(roleCopy, pos -> contains(transform, width, height, depth, pos));
        ownedSections = FootprintSupport.validatedOwnedSections(ownedSections,
                FootprintSupport.deriveBoxSections(transform, width, height, depth));
        watchedSections = FootprintSupport.immutableSections(watchedSections);
    }

    public BoxFootprint(StructureTransform transform, int width, int height, int depth,
                        Map<StructureRole, List<BlockPos>> roles) {
        this(transform, width, height, depth, Set.of(), Set.of(), roles);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return contains(transform, width, height, depth, pos);
    }

    @Override
    public long cellCount() {
        return Math.multiplyExact(Math.multiplyExact((long) width, height), depth);
    }

    @Override
    public Cursor cursor() {
        return cursor(transform, width, height, depth, roles);
    }

    @Override
    public List<BlockPos> positions(StructureRole role) {
        return FootprintSupport.positions(roles, role);
    }

    private static Cursor cursor(StructureTransform transform, int width, int height, int depth,
                                 Map<StructureRole, List<BlockPos>> roles) {
        return new FootprintSupport.AbstractCursor(transform, roles,
                Math.multiplyExact(Math.multiplyExact((long) width, height), depth)) {
            private int right;
            private int up;
            private int forward;

            @Override
            public Cell next() {
                Cell result = cell(right, up, forward);
                if (++right == width) {
                    right = 0;
                    if (++forward == depth) {
                        forward = 0;
                        up++;
                    }
                }
                return result;
            }
        };
    }

    private static boolean contains(StructureTransform transform, int width, int height, int depth, BlockPos pos) {
        StructureTransform.LocalPosition local = transform.toLocal(pos);
        return local.right() >= 0 && local.right() < width && local.up() >= 0 && local.up() < height
                && local.forward() >= 0 && local.forward() < depth;
    }
}
