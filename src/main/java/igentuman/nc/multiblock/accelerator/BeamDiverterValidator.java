package igentuman.nc.multiblock.accelerator;

import igentuman.nc.config.Multiblocks;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.api.multiblock.part.ElectromagnetDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public class BeamDiverterValidator extends AbstractMultiblockValidator<BeamDiverterCache> {

    private static final int SIZE = 5;
    private static final int CENTER = 2;
    private static final int LAST = SIZE - 1;

    private static final ResourceLocation EXPECTED_CASING = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_BEAM_PORT = rl("accelerator_beam_port");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_YOKE = rl("electromagnet_yoke");

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
    private int controllerCount;
    private final BlockPos[] beamPorts = new BlockPos[4];
    private ElectromagnetDef upMagnet;
    private ElectromagnetDef downMagnet;

    @Override
    protected boolean findBounds(BeamDiverterCache cache) {
        origin = null;
        forward = null;
        right = null;
        controllerCount = 0;
        upMagnet = null;
        downMagnet = null;
        for (int index = 0; index < beamPorts.length; index++) beamPorts[index] = null;

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
        step("detecting beam diverter bounds from {}", controllerPos);

        Direction inward = facing.getOpposite();
        Direction wallRight = facing.getCounterClockWise();

        int down = extent(cache, controllerPos, Direction.DOWN, SIZE);
        if (down < 0) return tooLarge(controllerPos, Direction.DOWN, SIZE);
        int up = extent(cache, controllerPos, Direction.UP, SIZE);
        if (up < 0) return tooLarge(controllerPos, Direction.UP, SIZE);
        int left = extent(cache, controllerPos, wallRight.getOpposite(), SIZE);
        if (left < 0) return tooLarge(controllerPos, wallRight.getOpposite(), SIZE);
        int rightExtent = extent(cache, controllerPos, wallRight, SIZE);
        if (rightExtent < 0) return tooLarge(controllerPos, wallRight, SIZE);

        BlockPos frontBottomLeft = controllerPos.below(down).relative(wallRight.getOpposite(), left);
        int depthExtent = extent(cache, frontBottomLeft, inward, SIZE);
        if (depthExtent < 0) return tooLarge(frontBottomLeft, inward, SIZE);

        int width = left + rightExtent + 1;
        int height = down + up + 1;
        int depth = depthExtent + 1;
        step("beam diverter extents width={} height={} depth={}", width, height, depth);
        if (width != SIZE || height != SIZE || depth != SIZE) {
            return fail("multiblock.discovery.wrong_size", controllerPos, EXPECTED_CASING, (ResourceLocation) null);
        }

        origin = frontBottomLeft;
        forward = inward;
        right = wallRight;

        BlockPos far = at(LAST, LAST, LAST);
        BlockPos min = new BlockPos(Math.min(origin.getX(), far.getX()), Math.min(origin.getY(), far.getY()),
                Math.min(origin.getZ(), far.getZ()));
        BlockPos max = new BlockPos(Math.max(origin.getX(), far.getX()), Math.max(origin.getY(), far.getY()),
                Math.max(origin.getZ(), far.getZ()));
        cache.setWorkingBounds(min, max);
        cache.setHeatCapacity(Math.multiplyExact((long) SIZE * SIZE * SIZE,
                Multiblocks.acceleratorHeatCapacityPerBlock));
        return true;
    }

    @Override
    protected boolean validateShell(BeamDiverterCache cache) {
        for (int u = 0; u < SIZE; u++) {
            for (int f = 0; f < SIZE; f++) {
                for (int r = 0; r < SIZE; r++) {
                    boolean boundaryRight = r == 0 || r == LAST;
                    boolean boundaryUp = u == 0 || u == LAST;
                    boolean boundaryForward = f == 0 || f == LAST;
                    int boundaryAxes = (boundaryRight ? 1 : 0) + (boundaryUp ? 1 : 0) + (boundaryForward ? 1 : 0);
                    if (boundaryAxes == 0) continue;
                    BlockPos pos = at(r, u, f);
                    BlockState state = cache.getBlockState(pos);
                    if (boundaryAxes >= 2) {
                        if (!state.is(casing)) {
                            return fail("multiblock.diverter.wrong_corner", pos, EXPECTED_CASING, state);
                        }
                        continue;
                    }
                    if (!validateShellCell(cache, pos, state, r, u, f)) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean validateInterior(BeamDiverterCache cache) {
        for (int u = 1; u < LAST; u++) {
            for (int f = 1; f < LAST; f++) {
                for (int r = 1; r < LAST; r++) {
                    BlockPos pos = at(r, u, f);
                    BlockState state = cache.getBlockState(pos);
                    if (!validateInnerCell(cache, pos, state, r, u, f)) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean validateRelations(BeamDiverterCache cache) {
        if (controllerCount != 1) {
            return fail("multiblock.diverter.controller_count", cache.controllerPos(),
                    rl("beam_diverter_controller"), (ResourceLocation) null);
        }
        for (BlockPos port : beamPorts) {
            if (port == null) {
                return fail("multiblock.diverter.beam_port_count", cache.controllerPos(), EXPECTED_BEAM_PORT,
                        (ResourceLocation) null);
            }
        }
        if (upMagnet == null || downMagnet == null) {
            return fail("multiblock.diverter.incomplete_magnet_pair", cache.controllerPos(), EXPECTED_YOKE,
                    (ResourceLocation) null);
        }
        cache.addBeamInput(beamPorts[0]);
        for (int index = 1; index < beamPorts.length; index++) cache.addBeamOutput(beamPorts[index]);
        return true;
    }

    private boolean validateShellCell(BeamDiverterCache cache, BlockPos pos, BlockState state, int r, int u, int f) {
        int portIndex = beamPortIndex(r, u, f);
        if (portIndex >= 0) {
            if (!state.is(beamPort)) {
                return fail("multiblock.diverter.missing_beam_port", pos, EXPECTED_BEAM_PORT, state);
            }
            Direction expected = switch (portIndex) {
                case 0 -> forward.getOpposite();
                case 1 -> right;
                case 2 -> forward;
                default -> right.getOpposite();
            };
            if (!faces(state, expected)) {
                return fail("multiblock.diverter.beam_port_facing", pos, EXPECTED_BEAM_PORT, state);
            }
            beamPorts[portIndex] = pos.immutable();
            cache.addWorkingPort(pos);
            return true;
        }
        if (state.is(beamPort)) {
            return fail("multiblock.diverter.unexpected_beam_port", pos, EXPECTED_CASING, state);
        }
        if (state.is(controller)) {
            if (!pos.equals(cache.controllerPos()) || !faces(state, cache.facing())) {
                return fail("multiblock.diverter.controller_position", pos, rl("beam_diverter_controller"), state);
            }
            controllerCount++;
            return true;
        }
        if (state.is(servicePort)) {
            cache.addServicePort(pos);
            cache.addWorkingPort(pos);
            return true;
        }
        if (!isShell(state)) return fail("multiblock.diverter.wrong_shell", pos, EXPECTED_CASING, state);
        return true;
    }

    private boolean validateInnerCell(BeamDiverterCache cache, BlockPos pos, BlockState state, int r, int u, int f) {
        boolean centerColumn = r == CENTER && f == CENTER;
        if (centerColumn && u == CENTER) {
            if (!state.is(particleBeam)) return fail("multiblock.diverter.wrong_beam", pos, EXPECTED_BEAM, state);
            return true;
        }
        boolean horizontalBeamNeighbor = u == CENTER
                && ((r == CENTER && (f == CENTER - 1 || f == CENTER + 1))
                || (f == CENTER && (r == CENTER - 1 || r == CENTER + 1)));
        if (horizontalBeamNeighbor) {
            if (!state.is(particleBeam)) return fail("multiblock.diverter.wrong_beam", pos, EXPECTED_BEAM, state);
            return true;
        }
        if (centerColumn && (u == CENTER - 1 || u == CENTER + 1)) {
            ElectromagnetDef magnet = AcceleratorComponents.electromagnet(state);
            if (magnet == null) return fail("multiblock.diverter.wrong_magnet", pos, EXPECTED_YOKE, state);
            cache.addEnergyPerTick(magnet.power);
            cache.addHeatPerTick(magnet.heat);
            cache.addDipoleField(magnet.magneticField / 2D);
            cache.includeMaximumTemperatureK(magnet.maxTemp / 1_000L);
            if (u == CENTER - 1) downMagnet = magnet;
            else upMagnet = magnet;
            if (upMagnet != null && downMagnet != null && !upMagnet.name.equals(downMagnet.name)) {
                return fail("multiblock.diverter.mixed_magnet_tier", pos, EXPECTED_YOKE, state);
            }
            return true;
        }
        if (!state.isAir() && !state.is(yoke)) {
            return fail("multiblock.diverter.wrong_inner", pos, EXPECTED_YOKE, state);
        }
        return true;
    }

    private static int beamPortIndex(int r, int u, int f) {
        if (u != CENTER) return -1;
        if (r == CENTER && f == 0) return 0;
        if (r == LAST && f == CENTER) return 1;
        if (r == CENTER && f == LAST) return 2;
        if (r == 0 && f == CENTER) return 3;
        return -1;
    }

    private int extent(BeamDiverterCache cache, BlockPos from, Direction direction, int limit) {
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
        controller = blockOf("beam_diverter_controller");
        servicePort = blockOf("accelerator_port");
        beamPort = blockOf("accelerator_beam_port");
        particleBeam = blockOf("particle_beam");
        yoke = blockOf("electromagnet_yoke");
        resolved = casing != null && casingGlass != null && controller != null && servicePort != null
                && beamPort != null && particleBeam != null && yoke != null;
        return resolved;
    }
}
