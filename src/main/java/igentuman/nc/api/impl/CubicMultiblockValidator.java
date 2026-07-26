package igentuman.nc.api.impl;

import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Validates an axis-aligned hollow-box multiblock by detecting its bounds, then its shell and interior. */
public class CubicMultiblockValidator implements IMultiblockValidator {

    private final BlockPredicate controllerPredicate;
    private final BlockPredicate shellPredicate;
    private final BlockPredicate interiorPredicate;
    private final int minWidth;
    private final int maxWidth;
    private final int minHeight;
    private final int maxHeight;
    private final int minDepth;
    private final int maxDepth;

    public CubicMultiblockValidator(BlockPredicate controllerPredicate,
                                    BlockPredicate shellPredicate,
                                    BlockPredicate interiorPredicate,
                                    int minWidth, int maxWidth,
                                    int minHeight, int maxHeight,
                                    int minDepth, int maxDepth) {
        this.controllerPredicate = controllerPredicate;
        this.shellPredicate = shellPredicate;
        this.interiorPredicate = interiorPredicate;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        int[] dims = detectDimensions(level, controllerPos, facing, cache);
        if (dims == null) {
            cache.getStructurePositions().clear();
            return false;
        }
        int w = dims[0], h = dims[1], d = dims[2];
        int cx = dims[3], cy = dims[4], cz = dims[5];
        cache.getStructurePositions().clear();
        if (!validateOuter(level, controllerPos, facing, cache, w, h, d, cx, cy, cz)) {
            cache.getStructurePositions().clear();
            return false;
        }
        if (!validateInner(level, controllerPos, facing, cache, w, h, d, cx, cy, cz)) {
            cache.getStructurePositions().clear();
            return false;
        }
        setStructureAABB(cache, controllerPos, facing, w, h, d, cx, cy, cz);
        return true;
    }

    /**
     * @return {@code {w, h, d, cx, cy, cz}} or {@code null} if dimensions can't be resolved
     *         within the configured ranges.
     */
    private int[] detectDimensions(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        int xNeg = scanShell(level, controllerPos, facing, cache, -1, 0, 0, maxWidth - 1);
        int xPos = scanShell(level, controllerPos, facing, cache, +1, 0, 0, maxWidth - 1);
        int yNeg = scanShell(level, controllerPos, facing, cache, 0, -1, 0, maxHeight - 1);
        int yPos = scanShell(level, controllerPos, facing, cache, 0, +1, 0, maxHeight - 1);
        int zNeg = scanShell(level, controllerPos, facing, cache, 0, 0, -1, maxDepth - 1);
        int zPos = scanShell(level, controllerPos, facing, cache, 0, 0, +1, maxDepth - 1);

        int w = xNeg + 1 + xPos;
        int h = yNeg + 1 + yPos;
        int d = zNeg + 1 + zPos;
        int cx = xNeg;
        int cy = yNeg;
        int cz = zNeg;

        if (w == 1) {
            int[] r = probeNormalAxis(level, controllerPos, facing, cache, 1, 0, 0, maxWidth - 1);
            if (r == null) return null;
            w = r[0];
            cx = r[1];
        }
        if (h == 1) {
            int[] r = probeNormalAxis(level, controllerPos, facing, cache, 0, 1, 0, maxHeight - 1);
            if (r == null) return null;
            h = r[0];
            cy = r[1];
        }
        if (d == 1) {
            int[] r = probeNormalAxis(level, controllerPos, facing, cache, 0, 0, 1, maxDepth - 1);
            if (r == null) return null;
            d = r[0];
            cz = r[1];
        }

        if (w < minWidth || w > maxWidth) return null;
        if (h < minHeight || h > maxHeight) return null;
        if (d < minDepth || d > maxDepth) return null;
        return new int[]{w, h, d, cx, cy, cz};
    }

    /** Walks (dx, dy, dz) per step from the controller; returns the count of consecutive
     *  cells matching {@code shellPredicate}, stopping at the first non-match. */
    private int scanShell(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache,
                          int dx, int dy, int dz, int maxSteps) {
        int count = 0;
        for (int k = 1; k <= maxSteps; k++) {
            BlockPos p = worldPos(controllerPos, facing, dx * k, dy * k, dz * k);
            BlockState s = cache.getBlockState(level, p);
            if (!shellPredicate.test(s, null)) break;
            count = k;
        }
        return count;
    }

    /** Walks +dir then -dir up to {@code maxSteps}; on the first shell-predicate match returns
     *  {@code {dim, controllerLocal}}. */
    private int[] probeNormalAxis(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache,
                                  int ax, int ay, int az, int maxSteps) {
        for (int k = 1; k <= maxSteps; k++) {
            BlockPos p = worldPos(controllerPos, facing, ax * k, ay * k, az * k);
            BlockState s = cache.getBlockState(level, p);
            if (shellPredicate.test(s, null)) return new int[]{k + 1, 0};
        }
        for (int k = 1; k <= maxSteps; k++) {
            BlockPos p = worldPos(controllerPos, facing, -ax * k, -ay * k, -az * k);
            BlockState s = cache.getBlockState(level, p);
            if (shellPredicate.test(s, null)) return new int[]{k + 1, k};
        }
        return null;
    }

    private boolean validateOuter(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache,
                                  int width, int height, int depth, int cx, int cy, int cz) {
        int wMax = width - 1, hMax = height - 1, dMax = depth - 1;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    if (x != 0 && x != wMax && y != 0 && y != hMax && z != 0 && z != dMax) continue;
                    BlockPos pos = worldPos(controllerPos, facing, x - cx, y - cy, z - cz);
                    BlockState state = cache.getBlockState(level, pos);
                    boolean isController = (x == cx && y == cy && z == cz);
                    if (!isController && controllerPredicate.test(state, null)) return false;
                    BlockPredicate predicate = isController ? controllerPredicate : shellPredicate;
                    if (!predicate.test(state, null)) return false;
                    cache.getBlockEntity(level, pos);
                }
            }
        }
        return true;
    }

    private boolean validateInner(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache,
                                  int width, int height, int depth, int cx, int cy, int cz) {
        int wMax = width - 1, hMax = height - 1, dMax = depth - 1;
        for (int y = 1; y < hMax; y++) {
            for (int z = 1; z < dMax; z++) {
                for (int x = 1; x < wMax; x++) {
                    BlockPos pos = worldPos(controllerPos, facing, x - cx, y - cy, z - cz);
                    BlockState state = cache.getBlockState(level, pos);
                    if (controllerPredicate.test(state, null)) return false;
                    if (!interiorPredicate.test(state, null)) return false;
                    cache.getBlockEntity(level, pos);
                }
            }
        }
        return true;
    }

    /** Computes the world-space AABB from the 8 corners of the local bounding box and sets it on the cache. */
    private static void setStructureAABB(IMultiblockCache cache, BlockPos controllerPos, Direction facing,
                                         int width, int height, int depth, int cx, int cy, int cz) {
        int wMax = width - 1, hMax = height - 1, dMax = depth - 1;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int[][] corners = {
                {0, 0, 0}, {wMax, 0, 0}, {0, hMax, 0}, {wMax, hMax, 0},
                {0, 0, dMax}, {wMax, 0, dMax}, {0, hMax, dMax}, {wMax, hMax, dMax}
        };
        for (int[] c : corners) {
            BlockPos wp = worldPos(controllerPos, facing, c[0] - cx, c[1] - cy, c[2] - cz);
            minX = Math.min(minX, wp.getX());
            minY = Math.min(minY, wp.getY());
            minZ = Math.min(minZ, wp.getZ());
            maxX = Math.max(maxX, wp.getX());
            maxY = Math.max(maxY, wp.getY());
            maxZ = Math.max(maxZ, wp.getZ());
        }
        cache.setAABB(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    private static BlockPos worldPos(BlockPos controllerPos, Direction facing, int dx, int dy, int dz) {
        return controllerPos.offset(rotate(dx, dz, facing, true), dy, rotate(dx, dz, facing, false));
    }

    private static int rotate(int dx, int dz, Direction facing, boolean returnX) {
        return switch (facing) {
            case SOUTH -> returnX ? -dx : -dz;
            case WEST -> returnX ? dz : -dx;
            case EAST -> returnX ? -dz : dx;
            default -> returnX ? dx : dz;
        };
    }
}
