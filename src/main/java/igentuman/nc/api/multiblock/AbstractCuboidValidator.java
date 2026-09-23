package igentuman.nc.api.multiblock;

import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public abstract class AbstractCuboidValidator<C extends AbstractMultiblockCache> extends AbstractMultiblockValidator<C> {

    protected abstract boolean resolveBlocks();

    protected abstract int minSize();

    protected abstract int maxSize();

    protected abstract boolean isController(BlockState state);

    protected abstract boolean isShell(BlockState state);

    protected abstract boolean acceptShell(C cache, BlockPos pos, BlockState state, boolean corner);

    protected abstract boolean acceptInterior(C cache, BlockPos pos, BlockState state);

    @Override
    protected final boolean findBounds(C cache) {
        int max = maxSize();
        int min = minSize();
        BlockPos origin = cache.controllerPos();
        if (!resolveBlocks()) return unresolved(origin);
        step("detecting cuboid bounds from {}", origin);

        int xNeg = scan(cache, origin, -1, 0, 0, max - 1);
        int xPos = scan(cache, origin, 1, 0, 0, max - 1);
        int yNeg = scan(cache, origin, 0, -1, 0, max - 1);
        int yPos = scan(cache, origin, 0, 1, 0, max - 1);
        int zNeg = scan(cache, origin, 0, 0, -1, max - 1);
        int zPos = scan(cache, origin, 0, 0, 1, max - 1);

        int width = xNeg + 1 + xPos;
        int height = yNeg + 1 + yPos;
        int depth = zNeg + 1 + zPos;

        if (width == 1) {
            int[] probed = probe(cache, origin, 1, 0, 0, max - 1);
            if (probed == null) return unresolved(origin);
            width = probed[0];
            xNeg = probed[1];
        }
        if (height == 1) {
            int[] probed = probe(cache, origin, 0, 1, 0, max - 1);
            if (probed == null) return unresolved(origin);
            height = probed[0];
            yNeg = probed[1];
        }
        if (depth == 1) {
            int[] probed = probe(cache, origin, 0, 0, 1, max - 1);
            if (probed == null) return unresolved(origin);
            depth = probed[0];
            zNeg = probed[1];
        }

        if (width < min || width > max || height < min || height > max || depth < min || depth > max) {
            return unresolved(origin);
        }
        BlockPos minimum = origin.offset(-xNeg, -yNeg, -zNeg);
        cache.setWorkingBounds(minimum, minimum.offset(width - 1, height - 1, depth - 1));
        return true;
    }

    private boolean unresolved(BlockPos origin) {
        return fail("multiblock.validation.dimensions_unresolved", origin, rl("valid_dimensions"),
                (ResourceLocation) null);
    }

    private int scan(C cache, BlockPos origin, int dx, int dy, int dz, int maxSteps) {
        int count = 0;
        for (int step = 1; step <= maxSteps; step++) {
            BlockPos pos = origin.offset(dx * step, dy * step, dz * step);
            if (!isShell(cache.getBlockState(pos))) break;
            count = step;
        }
        return count;
    }

    private int[] probe(C cache, BlockPos origin, int ax, int ay, int az, int maxSteps) {
        for (int step = 1; step <= maxSteps; step++) {
            if (isShell(cache.getBlockState(origin.offset(ax * step, ay * step, az * step)))) {
                return new int[]{step + 1, 0};
            }
        }
        for (int step = 1; step <= maxSteps; step++) {
            if (isShell(cache.getBlockState(origin.offset(-ax * step, -ay * step, -az * step)))) {
                return new int[]{step + 1, step};
            }
        }
        return null;
    }

    @Override
    protected final boolean validateShell(C cache) {
        BlockPos min = cache.workingMin();
        BlockPos max = cache.workingMax();
        BlockPos origin = cache.controllerPos();
        step("validating cuboid shell {} -> {}", min, max);
        for (int x = min.getX(); x <= max.getX(); x++) {
            boolean edgeX = x == min.getX() || x == max.getX();
            for (int y = min.getY(); y <= max.getY(); y++) {
                boolean edgeY = y == min.getY() || y == max.getY();
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    boolean edgeZ = z == min.getZ() || z == max.getZ();
                    if (!edgeX && !edgeY && !edgeZ) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = cache.getBlockState(pos);
                    if (pos.equals(origin)) {
                        if (!isController(state)) {
                            return fail("multiblock.validation.wrong_controller", pos, rl("multiblock_controller"), state);
                        }
                        continue;
                    }
                    if (isController(state)) {
                        return fail("multiblock.validation.extra_controller", pos, rl("multiblock_shell"), state);
                    }
                    if (!acceptShell(cache, pos, state, edgeX && edgeY && edgeZ)) return false;
                    collectPort(cache, pos);
                }
            }
        }
        return true;
    }

    @Override
    protected final boolean validateInterior(C cache) {
        BlockPos min = cache.workingMin();
        BlockPos max = cache.workingMax();
        for (int x = min.getX() + 1; x < max.getX(); x++) {
            for (int y = min.getY() + 1; y < max.getY(); y++) {
                for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = cache.getBlockState(pos);
                    if (isController(state)) {
                        return fail("multiblock.validation.extra_controller", pos, rl("multiblock_interior"), state);
                    }
                    if (!acceptInterior(cache, pos, state)) return false;
                    collectPort(cache, pos);
                }
            }
        }
        return true;
    }

    private void collectPort(C cache, BlockPos pos) {
        if (cache.getBlockEntity(pos) instanceof MultiblockPortBE) cache.addWorkingPort(pos);
    }
}
