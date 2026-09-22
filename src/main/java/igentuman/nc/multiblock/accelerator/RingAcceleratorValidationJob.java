package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.geometry.StructureTransform;
import igentuman.nc.multiblock.fusion.ElectromagnetDef;
import igentuman.nc.multiblock.fusion.RFAmplifierDef;
import igentuman.nc.multiblock.validation.ValidationResult;
import igentuman.nc.multiblock.validation.ValidationStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static igentuman.nc.NuclearCraft.rl;

public final class RingAcceleratorValidationJob extends AcceleratorValidationJob {

    public static final int MINIMUM_OUTER_SIDE = 11;
    public static final int WALL_OFFSET = 4;

    private static final ResourceLocation EXPECTED_CASING = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_SHELL = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_SLICE = rl("accelerator_slice");
    private static final int[][] COMPONENT_OFFSETS = {
            {2, 1}, {2, 3}, {1, 2}, {3, 2}, {3, 3}, {1, 1}, {1, 3}, {3, 1}
    };

    private final SquareRingFootprint ring;
    private final RingAcceleratorValidationContext context;
    private final int outerSide;
    private final int last;
    private final int topUp;
    private final Map<Long, SliceAccumulator> straightSlices = new HashMap<>();
    private final CornerAccumulator[] corners = {
            new CornerAccumulator(), new CornerAccumulator(), new CornerAccumulator(), new CornerAccumulator()
    };
    private final List<BlockPos> beamPorts = new ArrayList<>();
    private int controllerCount;
    private int componentCount;
    private double componentEfficiency;

    public RingAcceleratorValidationJob(UUID structureId, RingAcceleratorValidationContext context) {
        super(structureId, context.source());
        if (!(context.source().footprint() instanceof SquareRingFootprint squareRing)) {
            throw new IllegalArgumentException("Ring accelerator requires a square-ring footprint");
        }
        if (squareRing.wallOffset() != WALL_OFFSET) {
            throw new IllegalArgumentException("Ring accelerator requires a wall offset of " + WALL_OFFSET);
        }
        if (squareRing.outerSide() < MINIMUM_OUTER_SIDE) {
            throw new IllegalArgumentException("Ring accelerator outer side must be at least " + MINIMUM_OUTER_SIDE);
        }
        if (squareRing.height() != 5) {
            throw new IllegalArgumentException("Ring accelerator height must be exactly 5");
        }
        this.ring = squareRing;
        this.context = context;
        this.outerSide = squareRing.outerSide();
        this.last = outerSide - 1;
        this.topUp = squareRing.height() - 1;
        recordBuilder.aggregates()
                .beamLength(Math.multiplyExact(4, outerSide - 5))
                .heatCapacity(Math.multiplyExact(ring.cellCount(), context.heatCapacityPerBlock()));
    }

    @Override
    protected ValidationFailure validateLoadedCell(StructureFootprint.Cell cell, BlockState state,
                                                     @Nullable BlockEntity blockEntity) {
        StructureTransform.LocalPosition local = ring.transform().toLocal(cell.pos());
        int right = local.right();
        int up = local.up();
        int forward = local.forward();

        boolean boundaryRight = right == 0 || right == last;
        boolean boundaryUp = up == 0 || up == topUp;
        boolean boundaryForward = forward == 0 || forward == last;
        int boundaryAxes = (boundaryRight ? 1 : 0) + (boundaryUp ? 1 : 0) + (boundaryForward ? 1 : 0);

        if (boundaryAxes >= 2) {
            if (!context.cornerCasing().test(state)) {
                return failure("multiblock.ring.wrong_corner", EXPECTED_CASING, state);
            }
            return null;
        }
        if (boundaryAxes == 1) {
            return validateShell(cell.pos(), state, boundaryRight, boundaryForward, right, forward, up);
        }

        boolean rightBand = right < WALL_OFFSET || right >= outerSide - WALL_OFFSET;
        boolean forwardBand = forward < WALL_OFFSET || forward >= outerSide - WALL_OFFSET;
        if (rightBand && forwardBand) {
            return validateCorner(cell.pos(), state, right, up, forward);
        }
        return validateStraight(cell.pos(), state, right, up, forward, rightBand);
    }

    private ValidationFailure validateShell(BlockPos position, BlockState state, boolean boundaryRight,
                                             boolean boundaryForward, int right, int forward, int up) {
        Direction outward = null;
        if (boundaryRight) {
            outward = right == 0 ? ring.transform().right().getOpposite() : ring.transform().right();
        } else if (boundaryForward) {
            outward = forward == 0 ? ring.transform().forward().getOpposite() : ring.transform().forward();
        }

        if (outward != null && up == 2 && context.beamPort().test(state)) {
            if (!facesOutward(state, outward)) {
                return failure("multiblock.ring.beam_port_facing", EXPECTED_SHELL, state);
            }
            beamPorts.add(position.immutable());
            return null;
        }
        if (context.controller().test(state)) {
            if (outward == null || !position.equals(source.controllerPos())
                    || !facesOutward(state, source.orientation())) {
                return failure("multiblock.ring.controller_position", rl("ring_accelerator_controller"), state);
            }
            controllerCount++;
            return null;
        }
        if (context.servicePort().test(state)) {
            recordBuilder.role(StructureRole.SERVICE_PORT, position);
            return null;
        }
        if (context.beamPort().test(state)) {
            return failure("multiblock.ring.unexpected_beam_port", EXPECTED_SHELL, state);
        }
        if (!context.shell().test(state)) return failure("multiblock.ring.wrong_shell", EXPECTED_SHELL, state);
        return null;
    }

    private ValidationFailure validateCorner(BlockPos position, BlockState state, int right, int up, int forward) {
        int cornerIndex = (right < WALL_OFFSET ? 0 : 1) | (forward < WALL_OFFSET ? 0 : 2);
        int mirrorRight = right < WALL_OFFSET ? right : last - right;
        int mirrorForward = forward < WALL_OFFSET ? forward : last - forward;
        CornerAccumulator corner = corners[cornerIndex];

        if (mirrorRight == 2 && mirrorForward == 2) {
            if (up == 2) {
                if (!context.particleBeam().test(state)) return failure("multiblock.ring.wrong_beam", EXPECTED_BEAM, state);
                recordBuilder.role(StructureRole.PARTICLE_BEAM, position);
                return null;
            }
            if (up != 1 && up != 3) return failure("multiblock.ring.wrong_corner_dipole", EXPECTED_SLICE, state);
            ElectromagnetDef magnet = context.electromagnet().apply(state);
            if (magnet == null) return failure("multiblock.ring.wrong_corner_dipole", EXPECTED_SLICE, state);
            recordBuilder.role(StructureRole.ELECTROMAGNET, position);
            recordBuilder.aggregates().addEnergyPerTick(magnet.power).addHeatPerTick(magnet.heat)
                    .includeMaximumTemperatureK(magnet.maxTemp / 1_000L);
            componentEfficiency += magnet.efficiency / 100D;
            componentCount++;
            if (up == 1) {
                corner.down = magnet;
            } else {
                corner.up = magnet;
            }
            if (corner.up != null && corner.down != null && !corner.up.name.equals(corner.down.name)) {
                return new ValidationFailure("multiblock.ring.mixed_dipole_tier", EXPECTED_SLICE, EXPECTED_SLICE);
            }
            if (corner.up != null && corner.down != null) {
                recordBuilder.aggregates().addDipoleField(
                        (corner.up.magneticField + corner.down.magneticField) / 2D);
            }
            return null;
        }

        // Horizontal dipole faces can carry the ring beam or its connection to a beam port.
        boolean beamFace = up == 2 && (mirrorRight == 2 || mirrorForward == 2);
        if (beamFace && context.particleBeam().test(state)) {
            recordBuilder.role(StructureRole.PARTICLE_BEAM, position);
            return null;
        }
        if (!context.yoke().test(state)) {
            return failure("multiblock.ring.wrong_corner_fill", rl("electromagnet_yoke"), state);
        }
        return null;
    }

    private ValidationFailure validateStraight(BlockPos position, BlockState state, int right, int up, int forward,
                                                boolean rightBand) {
        int r;
        long bandId;
        long p;
        if (rightBand) {
            boolean west = right < WALL_OFFSET;
            r = west ? right : last - right;
            bandId = west ? 0 : 1;
            p = forward;
        } else {
            boolean south = forward < WALL_OFFSET;
            r = south ? forward : last - forward;
            bandId = south ? 2 : 3;
            p = right;
        }

        if (r == 2 && up == 2) {
            if (!context.particleBeam().test(state)) return failure("multiblock.ring.wrong_beam", EXPECTED_BEAM, state);
            recordBuilder.role(StructureRole.PARTICLE_BEAM, position);
            return null;
        }

        int componentIndex = componentIndex(r, up);
        if (componentIndex < 0) return failure("multiblock.ring.wrong_inner", EXPECTED_SLICE, state);
        Component component = classify(position, state);
        if (component == null) return failure("multiblock.ring.wrong_inner", EXPECTED_SLICE, state);

        long key = (bandId << 32) | (p & 0xFFFFFFFFL);
        SliceAccumulator accumulator = straightSlices.computeIfAbsent(key, ignored -> new SliceAccumulator());
        accumulator.cells[componentIndex] = component;
        accumulator.filled++;
        if (accumulator.filled == accumulator.cells.length) {
            straightSlices.remove(key);
            return validateSlice(accumulator.cells);
        }
        return null;
    }

    private Component classify(BlockPos position, BlockState state) {
        if (state.isAir()) return new Component(Kind.FILLER, "air", 0);
        ElectromagnetDef magnet = context.electromagnet().apply(state);
        if (magnet != null) {
            recordBuilder.role(StructureRole.ELECTROMAGNET, position);
            recordBuilder.aggregates().addEnergyPerTick(magnet.power).addHeatPerTick(magnet.heat)
                    .includeMaximumTemperatureK(magnet.maxTemp / 1_000L);
            componentEfficiency += magnet.efficiency / 100D;
            componentCount++;
            return new Component(Kind.MAGNET, magnet.name, magnet.magneticField);
        }
        RFAmplifierDef amplifier = context.rfAmplifier().apply(state);
        if (amplifier != null) {
            recordBuilder.role(StructureRole.RF_AMPLIFIER, position);
            recordBuilder.aggregates().addEnergyPerTick(amplifier.power).addHeatPerTick(amplifier.heat)
                    .includeMaximumTemperatureK(amplifier.maxTemp / 1_000L);
            componentEfficiency += amplifier.efficiency / 100D;
            componentCount++;
            return new Component(Kind.AMPLIFIER, amplifier.name, amplifier.voltage);
        }
        CoolerDef cooler = context.cooler().apply(state);
        if (cooler != null) {
            recordBuilder.role(StructureRole.COOLER, position);
            recordBuilder.aggregates().addCoolingPerTick(cooler.coolingPerTick());
            return new Component(Kind.FILLER, cooler.id().toString(), 0);
        }
        return context.yoke().test(state) ? new Component(Kind.FILLER, "yoke", 0) : null;
    }

    private ValidationFailure validateSlice(Component[] slice) {
        long magnets = Arrays.stream(slice).filter(component -> component.kind() == Kind.MAGNET).count();
        long amplifiers = Arrays.stream(slice).filter(component -> component.kind() == Kind.AMPLIFIER).count();
        if (magnets > 0 && amplifiers > 0) {
            return new ValidationFailure("multiblock.ring.mixed_optics", EXPECTED_SLICE, EXPECTED_SLICE);
        }
        if (amplifiers > 0) {
            if (amplifiers != 8) return new ValidationFailure("multiblock.ring.incomplete_rf_slice", EXPECTED_SLICE, EXPECTED_SLICE);
            double voltage = Arrays.stream(slice).mapToDouble(Component::value).sum() / 8D;
            recordBuilder.aggregates().addVoltage(Math.round(voltage));
            return null;
        }
        if (magnets == 0) return null;
        boolean verticalPair = magnet(slice, 0) && magnet(slice, 1);
        boolean horizontalPair = magnet(slice, 2) && magnet(slice, 3);
        boolean diagonalMagnet = magnet(slice, 4) || magnet(slice, 5) || magnet(slice, 6) || magnet(slice, 7);
        if (magnets == 4 && verticalPair && horizontalPair && !diagonalMagnet) {
            recordBuilder.aggregates().addQuadrupoleField(Arrays.stream(slice, 0, 4)
                    .mapToDouble(Component::value).average().orElse(0));
            return null;
        }
        if (magnets == 2 && !diagonalMagnet && (verticalPair ^ horizontalPair)) {
            Component first = verticalPair ? slice[0] : slice[2];
            Component second = verticalPair ? slice[1] : slice[3];
            if (!first.tier().equals(second.tier())) {
                return new ValidationFailure("multiblock.ring.mixed_dipole_tier", EXPECTED_SLICE, EXPECTED_SLICE);
            }
            recordBuilder.aggregates().addDipoleField((first.value() + second.value()) / 2D);
            return null;
        }
        return new ValidationFailure("multiblock.ring.invalid_magnet_slice", EXPECTED_SLICE, EXPECTED_SLICE);
    }

    private static boolean magnet(Component[] slice, int index) {
        return slice[index].kind() == Kind.MAGNET;
    }

    @Override
    protected ValidationResult completedResult(Map<Long, Long> sectionRevisions) {
        if (controllerCount != 1) return invalid("multiblock.ring.controller_count", source.controllerPos());
        if (beamPorts.size() < 2) return invalid("multiblock.ring.not_enough_beam_ports", source.controllerPos());
        for (CornerAccumulator corner : corners) {
            if (corner.up == null || corner.down == null) {
                return invalid("multiblock.ring.incomplete_corner_dipole", source.controllerPos());
            }
        }
        beamPorts.sort(java.util.Comparator.comparingInt(this::clockwisePortOrder));
        recordBuilder.role(StructureRole.BEAM_INPUT, beamPorts.get(0));
        for (int i = 1; i < beamPorts.size(); i++) recordBuilder.role(StructureRole.BEAM_OUTPUT, beamPorts.get(i));
        recordBuilder.aggregates()
                .efficiency(componentCount == 0 ? 0 : componentEfficiency / componentCount)
                .portChannels(beamPorts.size());
        StructureRecord record = recordBuilder.build(sectionRevisions);
        return new ValidationResult(ValidationStatus.VALID, "multiblock.validation.valid", null, null, null,
                footprint.cellCount(), footprint.cellCount(), null, record);
    }

    private ValidationResult invalid(String key, BlockPos position) {
        return new ValidationResult(ValidationStatus.INVALID, key, position, null, null,
                completedCells(), footprint.cellCount(), null, null);
    }

    private static boolean facesOutward(BlockState state, Direction expected) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private static int componentIndex(int right, int up) {
        for (int i = 0; i < COMPONENT_OFFSETS.length; i++) {
            if (COMPONENT_OFFSETS[i][0] == right && COMPONENT_OFFSETS[i][1] == up) return i;
        }
        return -1;
    }

    private int clockwisePortOrder(BlockPos position) {
        StructureTransform.LocalPosition local = ring.transform().toLocal(position);
        int right = local.right();
        int forward = local.forward();
        if (forward == 0) return right;
        if (right == last) return last + forward;
        if (forward == last) return 2 * last + (last - right);
        return 3 * last + (last - forward);
    }

    private static ValidationFailure failure(String key, ResourceLocation expected, BlockState actual) {
        return new ValidationFailure(key, expected, BuiltInRegistries.BLOCK.getKey(actual.getBlock()));
    }

    private enum Kind { MAGNET, AMPLIFIER, FILLER }

    private record Component(Kind kind, String tier, double value) {
    }

    private static final class SliceAccumulator {
        private final Component[] cells = new Component[8];
        private int filled;
    }

    private static final class CornerAccumulator {
        private ElectromagnetDef up;
        private ElectromagnetDef down;
    }
}
