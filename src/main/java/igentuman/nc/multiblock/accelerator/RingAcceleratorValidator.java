package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.multiblock.accelerator.AcceleratorSlice.Component;
import igentuman.nc.api.multiblock.part.ElectromagnetDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public class RingAcceleratorValidator extends AbstractMultiblockValidator<RingAcceleratorCache> {

    private static final ResourceLocation EXPECTED_CASING = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_SLICE = rl("accelerator_slice");
    private static final ResourceLocation EXPECTED_YOKE = rl("electromagnet_yoke");
    private static final ResourceLocation EXPECTED_BEAM_PORT = rl("accelerator_beam_port");
    private static final int HEIGHT = AcceleratorComponents.CROSS_SECTION;
    private static final int BAND = AcceleratorComponents.RING_WALL_OFFSET + 1;
    private static final int PREFETCH_SECTIONS = 32;

    private record BeamPort(BlockPos pos, int order) {
    }

    private static final class CornerDipole {
        private ElectromagnetDef up;
        private ElectromagnetDef down;
    }

    private Block casing;
    private Block casingGlass;
    private Block controller;
    private Block servicePort;
    private Block beamPort;
    private Block particleBeam;
    private Block yoke;
    private boolean resolved;

    private BlockPos origin;
    private Direction forward;
    private Direction right;
    private int outerSide;
    private int last;
    private int controllerCount;
    private final List<BeamPort> beamPorts = new ArrayList<>();
    private final CornerDipole[] corners = new CornerDipole[4];
    private final Component[] slice = new Component[AcceleratorSlice.OFFSETS.length];
    private final Map<BlockPos, CoolerDef> coolers = new HashMap<>();

    @Override
    protected boolean findBounds(RingAcceleratorCache cache) {
        origin = null;
        forward = null;
        right = null;
        outerSide = 0;
        last = 0;
        controllerCount = 0;
        beamPorts.clear();
        coolers.clear();
        for (int index = 0; index < corners.length; index++) corners[index] = new CornerDipole();

        BlockPos controllerPos = cache.controllerPos();
        if (!resolveBlocks()) {
            return fail("multiblock.validation.dimensions_unresolved", controllerPos, rl("valid_dimensions"),
                    (ResourceLocation) null);
        }
        Direction facing = cache.facing();
        if (!facing.getAxis().isHorizontal() || !isShell(cache.getBlockState(controllerPos))) {
            return fail("multiblock.discovery.controller_not_on_shell", controllerPos, EXPECTED_CASING,
                    cache.getBlockState(controllerPos));
        }
        step("detecting ring accelerator bounds from {}", controllerPos);

        Direction inward = facing.getOpposite();
        Direction wallRight = facing.getCounterClockWise();
        int[] range = AcceleratorComponents.ringSizeRange();
        int maximumSide = range[1];

        int down = extent(cache, controllerPos, Direction.DOWN, HEIGHT);
        if (down < 0) return tooLarge(controllerPos, Direction.DOWN, HEIGHT);
        int up = extent(cache, controllerPos, Direction.UP, HEIGHT);
        if (up < 0) return tooLarge(controllerPos, Direction.UP, HEIGHT);
        int left = extent(cache, controllerPos, wallRight.getOpposite(), maximumSide);
        if (left < 0) return tooLarge(controllerPos, wallRight.getOpposite(), maximumSide);
        int rightExtent = extent(cache, controllerPos, wallRight, maximumSide);
        if (rightExtent < 0) return tooLarge(controllerPos, wallRight, maximumSide);

        BlockPos frontBottomLeft = controllerPos.below(down).relative(wallRight.getOpposite(), left);
        int depthExtent = extent(cache, frontBottomLeft, inward, maximumSide);
        if (depthExtent < 0) return tooLarge(frontBottomLeft, inward, maximumSide);

        int width = left + rightExtent + 1;
        int height = down + up + 1;
        int depth = depthExtent + 1;
        step("ring accelerator extents width={} height={} depth={}", width, height, depth);

        if (width < range[0] || width > maximumSide || height != HEIGHT
                || depth < range[0] || depth > maximumSide) {
            return fail("multiblock.discovery.wrong_size", controllerPos, EXPECTED_CASING, (ResourceLocation) null);
        }
        if (width != depth || width < BAND * 2 + 1) {
            return fail("multiblock.discovery.expected_square_ring", controllerPos, EXPECTED_CASING,
                    (ResourceLocation) null);
        }

        origin = frontBottomLeft;
        forward = inward;
        right = wallRight;
        outerSide = width;
        last = outerSide - 1;

        BlockPos far = at(last, HEIGHT - 1, last);
        BlockPos min = new BlockPos(Math.min(origin.getX(), far.getX()), Math.min(origin.getY(), far.getY()),
                Math.min(origin.getZ(), far.getZ()));
        BlockPos max = new BlockPos(Math.max(origin.getX(), far.getX()), Math.max(origin.getY(), far.getY()),
                Math.max(origin.getZ(), far.getZ()));
        cache.setWorkingBounds(min, max);
        cache.setWorkingOuterSide(outerSide);
        cache.setWorkingBeamLength(Math.multiplyExact(4, outerSide - 5));
        long innerSide = outerSide - 2L * BAND;
        long cells = Math.multiplyExact(
                Math.subtractExact(Math.multiplyExact((long) outerSide, outerSide), innerSide * innerSide), HEIGHT);
        cache.setHeatCapacity(Math.multiplyExact(cells, Multiblocks.acceleratorHeatCapacityPerBlock));
        return true;
    }

    @Override
    protected void prefetchBounds(RingAcceleratorCache cache, BlockPos min, BlockPos max) {
        int batch = PREFETCH_SECTIONS * 16;
        for (int offset = 0; offset < outerSide; offset += batch) {
            int end = Math.min(last, offset + batch - 1);
            cache.prefetch(at(0, 0, offset), at(BAND - 1, HEIGHT - 1, end));
            cache.prefetch(at(last - BAND + 1, 0, offset), at(last, HEIGHT - 1, end));
            cache.prefetch(at(offset, 0, 0), at(end, HEIGHT - 1, BAND - 1));
            cache.prefetch(at(offset, 0, last - BAND + 1), at(end, HEIGHT - 1, last));
        }
    }

    @Override
    protected boolean validateShell(RingAcceleratorCache cache) {
        for (int u = 0; u < HEIGHT; u++) {
            for (int f = 0; f <= last; f++) {
                if (f < BAND || f > last - BAND) {
                    for (int r = 0; r <= last; r++) {
                        if (!visitShell(cache, r, u, f)) return false;
                    }
                    continue;
                }
                for (int r = 0; r < BAND; r++) {
                    if (!visitShell(cache, r, u, f)) return false;
                }
                for (int r = last - BAND + 1; r <= last; r++) {
                    if (!visitShell(cache, r, u, f)) return false;
                }
            }
        }
        return true;
    }

    private boolean visitShell(RingAcceleratorCache cache, int r, int u, int f) {
        if (isInteriorCell(r, u, f)) return true;
        return validateShellCell(cache, r, u, f);
    }

    private boolean isInteriorCell(int r, int u, int f) {
        if (u < 1 || u > HEIGHT - 2) return false;
        int br = bandCoordinate(r);
        int bf = bandCoordinate(f);
        if (br >= 0 && (br < 1 || br > BAND - 2)) return false;
        if (bf >= 0 && (bf < 1 || bf > BAND - 2)) return false;
        return br >= 0 || bf >= 0;
    }

    private int bandCoordinate(int value) {
        if (value < BAND) return value;
        if (value > last - BAND) return last - value;
        return -1;
    }

    @Override
    protected boolean validateInterior(RingAcceleratorCache cache) {
        for (int band = 0; band < 4; band++) {
            for (int p = BAND; p <= last - BAND; p++) {
                if (!validateStraightSlice(cache, band, p)) return false;
            }
        }
        for (int corner = 0; corner < 4; corner++) {
            for (int mr = 1; mr < BAND - 1; mr++) {
                for (int mf = 1; mf < BAND - 1; mf++) {
                    for (int u = 1; u < HEIGHT - 1; u++) {
                        if (!validateCornerCell(cache, corner, mr, mf, u)) return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected boolean validateRelations(RingAcceleratorCache cache) {
        if (controllerCount != 1) {
            return fail("multiblock.ring.controller_count", cache.controllerPos(), rl("ring_accelerator_controller"),
                    (ResourceLocation) null);
        }
        if (beamPorts.size() < 2) {
            return fail("multiblock.ring.not_enough_beam_ports", cache.controllerPos(), EXPECTED_BEAM_PORT,
                    (ResourceLocation) null);
        }
        for (CornerDipole corner : corners) {
            if (corner.up == null || corner.down == null) {
                return fail("multiblock.ring.incomplete_corner_dipole", cache.controllerPos(), EXPECTED_SLICE,
                        (ResourceLocation) null);
            }
        }
        beamPorts.sort(Comparator.comparingInt(BeamPort::order));
        cache.addBeamInput(beamPorts.getFirst().pos());
        for (int index = 1; index < beamPorts.size(); index++) cache.addBeamOutput(beamPorts.get(index).pos());
        AcceleratorSlice.applyCoolers(cache, coolers, this::insideRing);
        return true;
    }

    private boolean validateShellCell(RingAcceleratorCache cache, int r, int u, int f) {
        BlockPos pos = at(r, u, f);
        BlockState state = cache.getBlockState(pos);
        boolean boundaryRight = r == 0 || r == last;
        boolean boundaryUp = u == 0 || u == HEIGHT - 1;
        boolean boundaryForward = f == 0 || f == last;
        int boundaryAxes = (boundaryRight ? 1 : 0) + (boundaryUp ? 1 : 0) + (boundaryForward ? 1 : 0);
        if (boundaryAxes >= 2) {
            if (!state.is(casing)) return fail("multiblock.ring.wrong_corner", pos, EXPECTED_CASING, state);
            return true;
        }

        Direction outward = null;
        if (boundaryRight) outward = r == 0 ? right.getOpposite() : right;
        else if (boundaryForward) outward = f == 0 ? forward.getOpposite() : forward;

        if (outward != null && u == 2 && state.is(beamPort)) {
            if (!faces(state, outward)) {
                return fail("multiblock.ring.beam_port_facing", pos, EXPECTED_BEAM_PORT, state);
            }
            beamPorts.add(new BeamPort(pos.immutable(), clockwiseOrder(r, f)));
            cache.addWorkingPort(pos);
            return true;
        }
        if (state.is(controller)) {
            if (outward == null || !pos.equals(cache.controllerPos()) || !faces(state, cache.facing())) {
                return fail("multiblock.ring.controller_position", pos, rl("ring_accelerator_controller"), state);
            }
            controllerCount++;
            return true;
        }
        if (state.is(servicePort)) {
            cache.addServicePort(pos);
            cache.addWorkingPort(pos);
            return true;
        }
        if (state.is(beamPort)) {
            return fail("multiblock.ring.unexpected_beam_port", pos, EXPECTED_CASING, state);
        }
        if (!isShell(state)) return fail("multiblock.ring.wrong_shell", pos, EXPECTED_CASING, state);
        return true;
    }

    private boolean validateStraightSlice(RingAcceleratorCache cache, int band, int p) {
        BlockPos center = bandPos(band, 2, 2, p);
        BlockState beam = cache.getBlockState(center);
        if (!beam.is(particleBeam)) return fail("multiblock.ring.wrong_beam", center, EXPECTED_BEAM, beam);
        for (int index = 0; index < AcceleratorSlice.OFFSETS.length; index++) {
            BlockPos pos = bandPos(band, AcceleratorSlice.OFFSETS[index][0], AcceleratorSlice.OFFSETS[index][1], p);
            BlockState state = cache.getBlockState(pos);
            Component component = AcceleratorSlice.classify(cache, pos, state, yoke, coolers);
            if (component == null) return fail("multiblock.ring.wrong_inner", pos, EXPECTED_SLICE, state);
            slice[index] = component;
        }
        String failure = AcceleratorSlice.validate(cache, slice);
        if (failure == null) return true;
        return fail("multiblock.ring." + failure, center, EXPECTED_SLICE, EXPECTED_SLICE);
    }

    private boolean validateCornerCell(RingAcceleratorCache cache, int corner, int mr, int mf, int u) {
        int r = (corner & 1) == 0 ? mr : last - mr;
        int f = (corner & 2) == 0 ? mf : last - mf;
        BlockPos pos = at(r, u, f);
        BlockState state = cache.getBlockState(pos);

        if (mr == 2 && mf == 2) {
            if (u == 2) {
                if (!state.is(particleBeam)) return fail("multiblock.ring.wrong_beam", pos, EXPECTED_BEAM, state);
                return true;
            }
            ElectromagnetDef magnet = AcceleratorComponents.electromagnet(state);
            if (magnet == null) {
                return fail("multiblock.ring.wrong_corner_dipole", pos, EXPECTED_SLICE, state);
            }
            cache.addEnergyPerTick(magnet.power);
            cache.addHeatPerTick(magnet.heat);
            cache.includeMaximumTemperatureK(magnet.maxTemp / 1_000L);
            cache.addComponentEfficiency(magnet.efficiency / 100D);
            CornerDipole dipole = corners[corner];
            if (u == 1) dipole.down = magnet;
            else dipole.up = magnet;
            if (dipole.up != null && dipole.down != null) {
                if (!dipole.up.name.equals(dipole.down.name)) {
                    return fail("multiblock.ring.mixed_dipole_tier", pos, EXPECTED_SLICE, EXPECTED_SLICE);
                }
                cache.addDipoleField((dipole.up.magneticField + dipole.down.magneticField) / 2D);
            }
            return true;
        }

        boolean beamFace = u == 2 && (mr == 2 || mf == 2);
        if (beamFace && state.is(particleBeam)) return true;
        if (!state.is(yoke)) return fail("multiblock.ring.wrong_corner_fill", pos, EXPECTED_YOKE, state);
        return true;
    }

    private int clockwiseOrder(int r, int f) {
        if (f == 0) return r;
        if (r == last) return last + f;
        if (f == last) return 2 * last + (last - r);
        return 3 * last + (last - f);
    }

    private BlockPos bandPos(int band, int r, int u, int p) {
        return switch (band) {
            case 0 -> at(r, u, p);
            case 1 -> at(last - r, u, p);
            case 2 -> at(p, u, r);
            default -> at(p, u, last - r);
        };
    }

    private boolean insideRing(BlockPos pos) {
        int dx = pos.getX() - origin.getX();
        int dy = pos.getY() - origin.getY();
        int dz = pos.getZ() - origin.getZ();
        int localRight = dx * right.getStepX() + dz * right.getStepZ();
        int localForward = dx * forward.getStepX() + dz * forward.getStepZ();
        if (dy < 0 || dy >= HEIGHT || localRight < 0 || localRight > last
                || localForward < 0 || localForward > last) return false;
        return localRight < BAND || localRight > last - BAND
                || localForward < BAND || localForward > last - BAND;
    }

    private int extent(RingAcceleratorCache cache, BlockPos from, Direction direction, int limit) {
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

    private BlockPos at(int r, int u, int f) {
        return origin.relative(right, r).above(u).relative(forward, f);
    }

    private static boolean faces(BlockState state, Direction expected) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private boolean isShell(BlockState state) {
        return state.is(casing) || state.is(casingGlass) || state.is(controller)
                || state.is(servicePort) || state.is(beamPort);
    }

    private boolean resolveBlocks() {
        if (resolved) return true;
        casing = blockOf("accelerator_casing");
        casingGlass = blockOf("accelerator_casing_glass");
        controller = blockOf("ring_accelerator_controller");
        servicePort = blockOf("accelerator_port");
        beamPort = blockOf("accelerator_beam_port");
        particleBeam = blockOf("particle_beam");
        yoke = blockOf("electromagnet_yoke");
        resolved = casing != null && casingGlass != null && controller != null && servicePort != null
                && beamPort != null && particleBeam != null && yoke != null;
        return resolved;
    }
}
