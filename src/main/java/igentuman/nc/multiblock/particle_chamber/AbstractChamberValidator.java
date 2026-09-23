package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.multiblock.part.DetectorDef;
import igentuman.nc.block.particle.DetectorBlock;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public abstract class AbstractChamberValidator extends AbstractMultiblockValidator<ParticleChamberCache> {

    protected static final ResourceLocation EXPECTED_CASING = rl("target_chamber_casing");
    protected static final ResourceLocation EXPECTED_BEAM_PORT = rl("target_chamber_beam_port");
    protected static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    protected static final ResourceLocation EXPECTED_CAMERA = rl("target_chamber_camera");
    protected static final ResourceLocation EXPECTED_INNER = rl("target_chamber_inner");

    private final String prefix;
    private final String controllerName;

    protected Block casing;
    protected Block casingGlass;
    protected Block controller;
    protected Block servicePort;
    protected Block beamPort;
    protected Block particleBeam;
    protected Block camera;
    private boolean resolved;

    protected BlockPos origin;
    protected Direction right;
    protected Direction forward;
    protected int width;
    protected int height;
    protected int depth;
    private BlockPos controllerPosition = BlockPos.ZERO;
    private int controllerCount;

    protected AbstractChamberValidator(String machine, String controllerName) {
        this.prefix = "multiblock." + machine + ".";
        this.controllerName = controllerName;
    }

    protected abstract int maximumHeight();

    protected abstract int maximumLateral();

    protected abstract int maximumDepth();

    protected abstract boolean orient(BlockPos frontBottomLeft, Direction inward, Direction wallRight,
                                      int measuredWidth, int measuredHeight, int measuredDepth);

    protected abstract void resetPass();

    protected abstract boolean validateInnerCell(ParticleChamberCache cache, BlockPos pos, BlockState state,
                                                 int r, int u, int f);

    protected abstract boolean validateLayout(ParticleChamberCache cache);

    protected abstract long basePower();

    @Override
    protected final boolean findBounds(ParticleChamberCache cache) {
        origin = null;
        right = null;
        forward = null;
        width = 0;
        height = 0;
        depth = 0;
        controllerCount = 0;
        resetPass();

        BlockPos controllerPos = cache.controllerPos();
        controllerPosition = controllerPos;
        if (!resolveBlocks()) {
            return fail("multiblock.validation.dimensions_unresolved", controllerPos, rl("valid_dimensions"),
                    (ResourceLocation) null);
        }
        Direction facing = cache.facing();
        if (!facing.getAxis().isHorizontal() || !isShell(cache.getBlockState(controllerPos))) {
            return fail("multiblock.discovery.controller_not_on_shell", controllerPos, EXPECTED_CASING,
                    cache.getBlockState(controllerPos));
        }
        step("detecting {} bounds from {}", prefix, controllerPos);

        Direction inward = facing.getOpposite();
        Direction wallRight = facing.getCounterClockWise();
        int down = extent(cache, controllerPos, Direction.DOWN, maximumHeight());
        if (down < 0) return tooLarge(controllerPos, Direction.DOWN, maximumHeight());
        int up = extent(cache, controllerPos, Direction.UP, maximumHeight());
        if (up < 0) return tooLarge(controllerPos, Direction.UP, maximumHeight());
        int left = extent(cache, controllerPos, wallRight.getOpposite(), maximumLateral());
        if (left < 0) return tooLarge(controllerPos, wallRight.getOpposite(), maximumLateral());
        int rightExtent = extent(cache, controllerPos, wallRight, maximumLateral());
        if (rightExtent < 0) return tooLarge(controllerPos, wallRight, maximumLateral());
        BlockPos frontBottomLeft = controllerPos.below(down).relative(wallRight.getOpposite(), left);
        int depthExtent = extent(cache, frontBottomLeft, inward, maximumDepth());
        if (depthExtent < 0) return tooLarge(frontBottomLeft, inward, maximumDepth());

        int measuredWidth = left + rightExtent + 1;
        int measuredHeight = down + up + 1;
        int measuredDepth = depthExtent + 1;
        step("{} extents width={} height={} depth={}", prefix, measuredWidth, measuredHeight, measuredDepth);
        if (!orient(frontBottomLeft, inward, wallRight, measuredWidth, measuredHeight, measuredDepth)) return false;

        BlockPos far = at(width - 1, height - 1, depth - 1);
        cache.setWorkingBounds(
                new BlockPos(Math.min(origin.getX(), far.getX()), Math.min(origin.getY(), far.getY()),
                        Math.min(origin.getZ(), far.getZ())),
                new BlockPos(Math.max(origin.getX(), far.getX()), Math.max(origin.getY(), far.getY()),
                        Math.max(origin.getZ(), far.getZ())));
        return true;
    }

    protected final void frame(BlockPos frameOrigin, Direction frameRight, Direction frameForward,
                               int frameWidth, int frameHeight, int frameDepth) {
        origin = frameOrigin.immutable();
        right = frameRight;
        forward = frameForward;
        width = frameWidth;
        height = frameHeight;
        depth = frameDepth;
    }

    protected final boolean wrongSize(String key) {
        return fail(key, controllerPosition, EXPECTED_CASING, (ResourceLocation) null);
    }

    @Override
    protected final boolean validateShell(ParticleChamberCache cache) {
        for (int u = 0; u < height; u++) {
            for (int f = 0; f < depth; f++) {
                for (int r = 0; r < width; r++) {
                    if (!boundary(r, u, f)) continue;
                    BlockPos pos = at(r, u, f);
                    if (!validateShellCell(cache, pos, cache.getBlockState(pos), r, u, f)) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected final boolean validateInterior(ParticleChamberCache cache) {
        for (int u = 1; u < height - 1; u++) {
            for (int f = 1; f < depth - 1; f++) {
                for (int r = 1; r < width - 1; r++) {
                    BlockPos pos = at(r, u, f);
                    if (!validateInnerCell(cache, pos, cache.getBlockState(pos), r, u, f)) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected final boolean validateRelations(ParticleChamberCache cache) {
        if (controllerCount != 1) {
            return fail(prefix + "controller_count", cache.controllerPos(), rl(controllerName),
                    (ResourceLocation) null);
        }
        return validateLayout(cache);
    }

    @Override
    protected final void calculateStatistics(ParticleChamberCache cache) {
        cache.addEnergyPerTick(basePower());
    }

    protected boolean validateShellCell(ParticleChamberCache cache, BlockPos pos, BlockState state,
                                        int r, int u, int f) {
        if (boundaryAxes(r, u, f) >= 2 && !state.is(casing)) {
            return fail(prefix + "wrong_corner", pos, EXPECTED_CASING, state);
        }
        if (!isShell(state)) return fail(prefix + "wrong_shell", pos, EXPECTED_CASING, state);
        if (state.is(beamPort)) return fail(prefix + "unexpected_beam_port", pos, EXPECTED_CASING, state);
        if (state.is(controller)) {
            if (!pos.equals(cache.controllerPos()) || !faces(state, cache.facing())) {
                return fail(prefix + "controller_position", pos, rl(controllerName), state);
            }
            controllerCount++;
        } else if (state.is(servicePort)) {
            cache.addServicePort(pos);
            cache.addWorkingPort(pos);
        }
        return true;
    }

    protected final boolean countDetector(ParticleChamberCache cache, BlockState state, int distance) {
        DetectorDef detector = state.getBlock() instanceof DetectorBlock block ? block.definition() : null;
        if (detector == null) return false;
        if (distance <= detector.maximumDistance()) cache.addDetector(detector);
        return true;
    }

    protected final boolean validateFill(BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(particleBeam)) return true;
        return fail(prefix + "wrong_inner", pos, EXPECTED_INNER, state);
    }

    protected final boolean invalid(String suffix, BlockPos pos) {
        return fail(prefix + suffix, pos, null, (ResourceLocation) null);
    }

    protected final boolean invalid(String suffix, BlockPos pos, ResourceLocation expected, BlockState state) {
        return fail(prefix + suffix, pos, expected, state);
    }

    protected final BlockPos at(int r, int u, int f) {
        return origin.relative(right, r).above(u).relative(forward, f);
    }

    protected final boolean boundary(int r, int u, int f) {
        return boundaryAxes(r, u, f) > 0;
    }

    private int boundaryAxes(int r, int u, int f) {
        return (r == 0 || r == width - 1 ? 1 : 0) + (u == 0 || u == height - 1 ? 1 : 0)
                + (f == 0 || f == depth - 1 ? 1 : 0);
    }

    protected static boolean faces(BlockState state, @Nullable Direction expected) {
        return expected != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private boolean isShell(BlockState state) {
        return state.is(casing) || state.is(casingGlass) || state.is(controller)
                || state.is(servicePort) || state.is(beamPort);
    }

    private int extent(ParticleChamberCache cache, BlockPos from, Direction direction, int limit) {
        int distance = 1;
        while (true) {
            if (!isShell(cache.getBlockState(from.relative(direction, distance)))) return distance - 1;
            if (distance >= limit) return -1;
            distance++;
        }
    }

    private boolean tooLarge(BlockPos from, Direction direction, int limit) {
        return fail("multiblock.discovery.too_large", from.relative(direction, limit + 1),
                EXPECTED_CASING, (ResourceLocation) null);
    }

    private boolean resolveBlocks() {
        if (resolved) return true;
        casing = blockOf("target_chamber_casing");
        casingGlass = blockOf("target_chamber_casing_glass");
        controller = blockOf(controllerName);
        servicePort = blockOf("target_chamber_port");
        beamPort = blockOf("target_chamber_beam_port");
        particleBeam = blockOf("particle_beam");
        camera = blockOf("target_chamber_camera");
        resolved = casing != null && casingGlass != null && controller != null && servicePort != null
                && beamPort != null && particleBeam != null && camera != null;
        return resolved;
    }
}
