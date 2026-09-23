package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.multiblock.accelerator.AcceleratorSlice.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public class LinearAcceleratorValidator extends AbstractMultiblockValidator<LinearAcceleratorCache> {

    private static final ResourceLocation EXPECTED_CASING = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_ENDPOINT = rl("accelerator_beam_endpoint");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_SLICE = rl("accelerator_slice");
    private static final int CROSS_SECTION = AcceleratorComponents.CROSS_SECTION;
    private static final int PREFETCH_SECTIONS = 32;

    private Block casing;
    private Block casingGlass;
    private Block controller;
    private Block servicePort;
    private Block beamPort;
    private Block ionSourcePort;
    private Block particleBeam;
    private Block yoke;
    private boolean resolved;

    private BlockPos origin;
    private Direction forward;
    private Direction right;
    private int length;
    private final BlockPos[] endpoints = new BlockPos[2];
    private final boolean[] ionEndpoints = new boolean[2];
    private int controllerCount;
    private int beamPortCount;
    private int ionSourceCount;
    private final Component[] slice = new Component[AcceleratorSlice.OFFSETS.length];
    private final Map<BlockPos, CoolerDef> coolers = new HashMap<>();

    @Override
    protected boolean findBounds(LinearAcceleratorCache cache) {
        origin = null;
        forward = null;
        right = null;
        length = 0;
        endpoints[0] = null;
        endpoints[1] = null;
        ionEndpoints[0] = false;
        ionEndpoints[1] = false;
        controllerCount = 0;
        beamPortCount = 0;
        ionSourceCount = 0;
        coolers.clear();

        BlockPos controllerPos = cache.controllerPos();
        if (!resolveBlocks()) {
            return fail("multiblock.validation.dimensions_unresolved", controllerPos, rl("valid_dimensions"),
                    (ResourceLocation) null);
        }
        Direction facing = cache.facing();
        if (!facing.getAxis().isHorizontal()) {
            return fail("multiblock.discovery.controller_not_on_shell", controllerPos, EXPECTED_CASING,
                    cache.getBlockState(controllerPos));
        }
        step("detecting linear accelerator bounds from {}", controllerPos);
        if (!isShell(cache.getBlockState(controllerPos))) {
            return fail("multiblock.discovery.controller_not_on_shell", controllerPos, EXPECTED_CASING,
                    cache.getBlockState(controllerPos));
        }

        Direction inward = facing.getOpposite();
        Direction wallRight = facing.getCounterClockWise();
        int[] range = AcceleratorComponents.linearSizeRange();
        int maximumLong = range[1];

        int down = extent(cache, controllerPos, Direction.DOWN, CROSS_SECTION);
        if (down < 0) return tooLarge(controllerPos, Direction.DOWN, CROSS_SECTION);
        int up = extent(cache, controllerPos, Direction.UP, CROSS_SECTION);
        if (up < 0) return tooLarge(controllerPos, Direction.UP, CROSS_SECTION);
        int left = extent(cache, controllerPos, wallRight.getOpposite(), maximumLong);
        if (left < 0) return tooLarge(controllerPos, wallRight.getOpposite(), maximumLong);
        int rightExtent = extent(cache, controllerPos, wallRight, maximumLong);
        if (rightExtent < 0) return tooLarge(controllerPos, wallRight, maximumLong);

        BlockPos frontBottomLeft = controllerPos.below(down).relative(wallRight.getOpposite(), left);
        int depthExtent = extent(cache, frontBottomLeft, inward, maximumLong);
        if (depthExtent < 0) return tooLarge(frontBottomLeft, inward, maximumLong);

        int width = left + rightExtent + 1;
        int height = down + up + 1;
        int depth = depthExtent + 1;
        step("linear accelerator extents width={} height={} depth={}", width, height, depth);

        if (width < CROSS_SECTION || width > maximumLong || height != CROSS_SECTION
                || depth < CROSS_SECTION || depth > maximumLong) {
            return fail("multiblock.discovery.wrong_size", controllerPos, EXPECTED_CASING, (ResourceLocation) null);
        }
        if (width != CROSS_SECTION && depth != CROSS_SECTION) {
            return fail("multiblock.discovery.expected_linear_tube", controllerPos, EXPECTED_CASING,
                    (ResourceLocation) null);
        }
        length = Math.max(width, depth);
        if (length < range[0]) {
            return fail("multiblock.discovery.expected_linear_tube", controllerPos, EXPECTED_CASING,
                    (ResourceLocation) null);
        }
        forward = width == CROSS_SECTION ? inward : wallRight;
        right = width == CROSS_SECTION ? wallRight : inward;

        BlockPos far = frontBottomLeft.relative(wallRight, width - 1).above(height - 1).relative(inward, depth - 1);
        BlockPos min = new BlockPos(Math.min(frontBottomLeft.getX(), far.getX()),
                Math.min(frontBottomLeft.getY(), far.getY()), Math.min(frontBottomLeft.getZ(), far.getZ()));
        BlockPos max = new BlockPos(Math.max(frontBottomLeft.getX(), far.getX()),
                Math.max(frontBottomLeft.getY(), far.getY()), Math.max(frontBottomLeft.getZ(), far.getZ()));
        origin = localOrigin(min, max);
        cache.setWorkingBounds(min, max);
        cache.setWorkingForward(forward);
        cache.setWorkingBeamLength(length - 2);
        cache.setHeatCapacity(Math.multiplyExact((long) length * CROSS_SECTION * CROSS_SECTION,
                Multiblocks.acceleratorHeatCapacityPerBlock));
        return true;
    }

    @Override
    protected void prefetchBounds(LinearAcceleratorCache cache, BlockPos min, BlockPos max) {
        int batch = PREFETCH_SECTIONS * 16;
        for (int offset = 0; offset < length; offset += batch) {
            BlockPos from = at(0, 0, offset);
            BlockPos to = at(CROSS_SECTION - 1, CROSS_SECTION - 1, Math.min(length - 1, offset + batch - 1));
            cache.prefetch(from, to);
        }
    }

    @Override
    protected boolean validateShell(LinearAcceleratorCache cache) {
        int last = length - 1;
        for (int f = 0; f <= last; f++) {
            boolean cap = f == 0 || f == last;
            for (int u = 0; u < CROSS_SECTION; u++) {
                for (int r = 0; r < CROSS_SECTION; r++) {
                    boolean ring = r == 0 || r == CROSS_SECTION - 1 || u == 0 || u == CROSS_SECTION - 1;
                    if (!cap && !ring) continue;
                    BlockPos pos = at(r, u, f);
                    BlockState state = cache.getBlockState(pos);
                    if (cap && r == 2 && u == 2) {
                        if (!validateEndpoint(cache, pos, state, f == 0)) return false;
                        continue;
                    }
                    if (!validateShellBlock(cache, pos, state, r, u, f, last)) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean validateInterior(LinearAcceleratorCache cache) {
        int last = length - 1;
        for (int f = 1; f < last; f++) {
            for (int index = 0; index < slice.length; index++) slice[index] = null;
            BlockPos center = at(2, 2, f);
            BlockState beam = cache.getBlockState(center);
            if (!beam.is(particleBeam)) return fail("multiblock.linear.wrong_beam", center, EXPECTED_BEAM, beam);
            for (int index = 0; index < AcceleratorSlice.OFFSETS.length; index++) {
                BlockPos pos = at(AcceleratorSlice.OFFSETS[index][0], AcceleratorSlice.OFFSETS[index][1], f);
                BlockState state = cache.getBlockState(pos);
                Component component = AcceleratorSlice.classify(cache, pos, state, yoke, coolers);
                if (component == null) return fail("multiblock.linear.wrong_inner", pos, EXPECTED_SLICE, state);
                slice[index] = component;
            }
            if (!validateSlice(cache, center)) return false;
        }
        return true;
    }

    @Override
    protected boolean validateRelations(LinearAcceleratorCache cache) {
        if (controllerCount != 1) {
            return fail("multiblock.linear.controller_count", cache.controllerPos(),
                    rl("linear_accelerator_controller"), (ResourceLocation) null);
        }
        if (ionSourceCount > 1 || beamPortCount + ionSourceCount != 2 || beamPortCount == 0) {
            return fail("multiblock.linear.endpoint_count", cache.controllerPos(), EXPECTED_ENDPOINT,
                    (ResourceLocation) null);
        }
        if (ionSourceCount == 1) {
            for (int index = 0; index < endpoints.length; index++) {
                if (ionEndpoints[index]) cache.addIonSource(endpoints[index]);
                else cache.addBeamOutput(endpoints[index]);
            }
        } else {
            cache.addBeamInput(endpoints[0]);
            cache.addBeamOutput(endpoints[1]);
        }
        BlockPos min = cache.workingMin();
        BlockPos max = cache.workingMax();
        AcceleratorSlice.applyCoolers(cache, coolers, pos -> within(pos, min, max));
        return true;
    }

    private boolean validateEndpoint(LinearAcceleratorCache cache, BlockPos pos, BlockState state, boolean first) {
        boolean beam = state.is(beamPort);
        boolean source = state.is(ionSourcePort);
        if (!beam && !source) return fail("multiblock.linear.wrong_endpoint", pos, EXPECTED_ENDPOINT, state);
        if (!faces(state, first ? forward.getOpposite() : forward)) {
            return fail("multiblock.linear.endpoint_facing", pos, EXPECTED_ENDPOINT, state);
        }
        if (beam) beamPortCount++;
        else ionSourceCount++;
        int index = first ? 0 : 1;
        endpoints[index] = pos.immutable();
        ionEndpoints[index] = source;
        cache.addWorkingPort(pos);
        return true;
    }

    private boolean validateShellBlock(LinearAcceleratorCache cache, BlockPos pos, BlockState state,
                                       int r, int u, int f, int last) {
        int boundaryAxes = (r == 0 || r == CROSS_SECTION - 1 ? 1 : 0)
                + (u == 0 || u == CROSS_SECTION - 1 ? 1 : 0)
                + (f == 0 || f == last ? 1 : 0);
        if (boundaryAxes >= 2 && !state.is(casing)) {
            return fail("multiblock.linear.wrong_corner", pos, EXPECTED_CASING, state);
        }
        if (!isShell(state)) return fail("multiblock.linear.wrong_shell", pos, EXPECTED_CASING, state);
        if (state.is(beamPort) || state.is(ionSourcePort)) {
            return fail("multiblock.linear.unexpected_endpoint", pos, EXPECTED_CASING, state);
        }
        if (state.is(controller)) {
            if (!pos.equals(cache.controllerPos()) || !faces(state, cache.facing())) {
                return fail("multiblock.linear.controller_position", pos, rl("linear_accelerator_controller"), state);
            }
            controllerCount++;
        } else if (state.is(servicePort)) {
            cache.addServicePort(pos);
            cache.addWorkingPort(pos);
        }
        return true;
    }

    private boolean validateSlice(LinearAcceleratorCache cache, BlockPos center) {
        String failure = AcceleratorSlice.validate(cache, slice);
        if (failure == null) return true;
        return fail("multiblock.linear." + failure, center, EXPECTED_SLICE, EXPECTED_SLICE);
    }

    private int extent(LinearAcceleratorCache cache, BlockPos from, Direction direction, int limit) {
        int distance = 1;
        while (true) {
            BlockPos probe = from.relative(direction, distance);
            if (!isShell(cache.getBlockState(probe))) return distance - 1;
            if (distance >= limit) return -1;
            distance++;
        }
    }

    private boolean tooLarge(BlockPos from, Direction direction, int limit) {
        return fail("multiblock.discovery.too_large", from.relative(direction, limit + 1),
                EXPECTED_CASING, (ResourceLocation) null);
    }

    private BlockPos localOrigin(BlockPos min, BlockPos max) {
        int x = min.getX();
        int y = min.getY();
        int z = min.getZ();
        for (Direction direction : new Direction[]{forward, right}) {
            if (direction.getAxisDirection() != Direction.AxisDirection.NEGATIVE) continue;
            if (direction.getAxis() == Direction.Axis.X) x = max.getX();
            else z = max.getZ();
        }
        return new BlockPos(x, y, z);
    }

    private BlockPos at(int r, int u, int f) {
        return origin.relative(right, r).above(u).relative(forward, f);
    }

    private static boolean within(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    private static boolean faces(BlockState state, Direction expected) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private boolean isShell(BlockState state) {
        return state.is(casing) || state.is(casingGlass) || state.is(controller)
                || state.is(servicePort) || state.is(beamPort) || state.is(ionSourcePort);
    }

    private boolean resolveBlocks() {
        if (resolved) return true;
        casing = blockOf("accelerator_casing");
        casingGlass = blockOf("accelerator_casing_glass");
        controller = blockOf("linear_accelerator_controller");
        servicePort = blockOf("accelerator_port");
        beamPort = blockOf("accelerator_beam_port");
        ionSourcePort = blockOf("accelerator_ion_source_port");
        particleBeam = blockOf("particle_beam");
        yoke = blockOf("electromagnet_yoke");
        resolved = casing != null && casingGlass != null && controller != null && servicePort != null
                && beamPort != null && ionSourcePort != null && particleBeam != null && yoke != null;
        return resolved;
    }
}
