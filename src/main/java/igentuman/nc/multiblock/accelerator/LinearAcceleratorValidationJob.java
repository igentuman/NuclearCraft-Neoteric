package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.MultiblockPersistence;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.LinearTubeFootprint;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public final class LinearAcceleratorValidationJob extends AcceleratorValidationJob {

    private static final ResourceLocation EXPECTED_CASING = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_SHELL = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_ENDPOINT = rl("accelerator_beam_endpoint");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_SLICE = rl("accelerator_slice");
    private static final int[][] COMPONENT_OFFSETS = {
            {2, 1}, {2, 3}, {1, 2}, {3, 2}, {3, 3}, {1, 1}, {1, 3}, {3, 1}
    };

    private final LinearTubeFootprint linearFootprint;
    private final LinearAcceleratorValidationContext context;
    private final Component[] slice = new Component[8];
    private final BlockPos[] endpoints = new BlockPos[2];
    private final boolean[] ionEndpoints = new boolean[2];
    private int controllerCount;
    private int beamPortCount;
    private int ionSourceCount;
    private int componentCount;
    private double componentEfficiency;
    private final Map<BlockPos, BlockState> scannedStates = new HashMap<>();
    private final Map<BlockPos, CoolerDef> coolers = new HashMap<>();

    public LinearAcceleratorValidationJob(UUID structureId, LinearAcceleratorValidationContext context) {
        super(structureId, context.source());
        if (!(context.source().footprint() instanceof LinearTubeFootprint linear)) {
            throw new IllegalArgumentException("Linear accelerator requires a linear-tube footprint");
        }
        this.linearFootprint = linear;
        this.context = context;
        recordBuilder.aggregates()
                .beamLength(Math.max(0, linear.length() - 2))
                .heatCapacity(Math.multiplyExact(linear.cellCount(), context.heatCapacityPerBlock()))
                .portChannels(2);
    }

    @Override
    protected ValidationFailure validateLoadedCell(StructureFootprint.Cell cell, BlockState state,
                                                     @Nullable BlockEntity blockEntity) {
        scannedStates.put(cell.pos().immutable(), state);
        StructureTransform.LocalPosition local = linearFootprint.transform().toLocal(cell.pos());
        int right = local.right();
        int up = local.up();
        int forward = local.forward();
        int last = linearFootprint.length() - 1;
        boolean boundary = right == 0 || right == 4 || up == 0 || up == 4 || forward == 0 || forward == last;
        boolean endpoint = right == 2 && up == 2 && (forward == 0 || forward == last);

        if (endpoint) return validateEndpoint(cell.pos(), state, forward == 0);
        if (boundary) return validateShell(cell.pos(), state, right, up, forward, last);
        if (right == 2 && up == 2) {
            if (!context.particleBeam().test(state)) return failure("multiblock.linear.wrong_beam", EXPECTED_BEAM, state);
            recordBuilder.role(StructureRole.PARTICLE_BEAM, cell.pos());
            return null;
        }

        int componentIndex = componentIndex(right, up);
        if (componentIndex < 0) return failure("multiblock.linear.wrong_inner", EXPECTED_SLICE, state);
        Component component = classify(cell.pos(), state);
        if (component == null) return failure("multiblock.linear.wrong_inner", EXPECTED_SLICE, state);
        slice[componentIndex] = component;
        if (componentIndex == 4) {
            ValidationFailure sliceFailure = validateSlice();
            Arrays.fill(slice, null);
            return sliceFailure;
        }
        return null;
    }

    private ValidationFailure validateEndpoint(BlockPos position, BlockState state, boolean first) {
        boolean beam = context.beamPort().test(state);
        boolean sourcePort = context.ionSourcePort().test(state);
        if (!beam && !sourcePort) return failure("multiblock.linear.wrong_endpoint", EXPECTED_ENDPOINT, state);
        if (!facesOutward(state, first ? linearFootprint.transform().forward().getOpposite()
                : linearFootprint.transform().forward())) {
            return failure("multiblock.linear.endpoint_facing", EXPECTED_ENDPOINT, state);
        }
        if (beam) {
            beamPortCount++;
        } else {
            ionSourceCount++;
        }
        int index = first ? 0 : 1;
        endpoints[index] = position.immutable();
        ionEndpoints[index] = sourcePort;
        return null;
    }

    private ValidationFailure validateShell(BlockPos position, BlockState state, int right, int up,
                                             int forward, int last) {
        int boundaryAxes = (right == 0 || right == 4 ? 1 : 0)
                + (up == 0 || up == 4 ? 1 : 0)
                + (forward == 0 || forward == last ? 1 : 0);
        if (boundaryAxes >= 2 && !context.cornerCasing().test(state)) {
            return failure("multiblock.linear.wrong_corner", EXPECTED_CASING, state);
        }
        if (!context.shell().test(state)) return failure("multiblock.linear.wrong_shell", EXPECTED_SHELL, state);
        if ((context.beamPort().test(state) || context.ionSourcePort().test(state))) {
            return failure("multiblock.linear.unexpected_endpoint", EXPECTED_SHELL, state);
        }
        if (context.controller().test(state)) {
            if (!position.equals(source.controllerPos()) || !facesOutward(state, source.orientation())) {
                return failure("multiblock.linear.controller_position", rl("linear_accelerator_controller"), state);
            }
            controllerCount++;
        } else if (context.servicePort().test(state)) {
            recordBuilder.role(StructureRole.SERVICE_PORT, position);
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
            coolers.put(position.immutable(), cooler);
            return new Component(Kind.FILLER, cooler.id().toString(), 0);
        }
        return context.yoke().test(state) ? new Component(Kind.FILLER, "yoke", 0) : null;
    }

    private ValidationFailure validateSlice() {
        long magnets = Arrays.stream(slice).filter(component -> component.kind() == Kind.MAGNET).count();
        long amplifiers = Arrays.stream(slice).filter(component -> component.kind() == Kind.AMPLIFIER).count();
        if (magnets > 0 && amplifiers > 0) {
            return new ValidationFailure("multiblock.linear.mixed_optics", EXPECTED_SLICE, EXPECTED_SLICE);
        }
        if (amplifiers > 0) {
            if (amplifiers != 8) return new ValidationFailure("multiblock.linear.incomplete_rf_slice", EXPECTED_SLICE, EXPECTED_SLICE);
            double voltage = Arrays.stream(slice).mapToDouble(Component::value).sum() / 8D;
            recordBuilder.aggregates().addVoltage(Math.round(voltage));
            return null;
        }
        if (magnets == 0) return null;
        boolean verticalPair = magnet(0) && magnet(1);
        boolean horizontalPair = magnet(2) && magnet(3);
        boolean diagonalMagnet = magnet(4) || magnet(5) || magnet(6) || magnet(7);
        if (magnets == 4 && verticalPair && horizontalPair && !diagonalMagnet) {
            recordBuilder.aggregates().addQuadrupoleField(Arrays.stream(slice, 0, 4)
                    .mapToDouble(Component::value).average().orElse(0));
            return null;
        }
        if (magnets == 2 && !diagonalMagnet && (verticalPair ^ horizontalPair)) {
            Component first = verticalPair ? slice[0] : slice[2];
            Component second = verticalPair ? slice[1] : slice[3];
            if (!first.tier().equals(second.tier())) {
                return new ValidationFailure("multiblock.linear.mixed_dipole_tier", EXPECTED_SLICE, EXPECTED_SLICE);
            }
            recordBuilder.aggregates().addDipoleField((first.value() + second.value()) / 2D);
            return null;
        }
        return new ValidationFailure("multiblock.linear.invalid_magnet_slice", EXPECTED_SLICE, EXPECTED_SLICE);
    }

    private boolean magnet(int index) {
        return slice[index].kind() == Kind.MAGNET;
    }

    @Override
    protected ValidationResult completedResult(Map<Long, Long> sectionRevisions) {
        if (controllerCount != 1) return invalid("multiblock.linear.controller_count", source.controllerPos());
        if (ionSourceCount > 1 || beamPortCount + ionSourceCount != 2 || beamPortCount == 0) {
            return invalid("multiblock.linear.endpoint_count", source.controllerPos());
        }
        if (ionSourceCount == 1) {
            for (int i = 0; i < endpoints.length; i++) {
                recordBuilder.role(ionEndpoints[i] ? StructureRole.ION_SOURCE : StructureRole.BEAM_OUTPUT,
                        endpoints[i]);
            }
        } else {
            recordBuilder.role(StructureRole.BEAM_INPUT, endpoints[0]);
            recordBuilder.role(StructureRole.BEAM_OUTPUT, endpoints[1]);
        }
        for (var entry : coolers.entrySet()) {
            var neighbors = Arrays.stream(Direction.values())
                    .map(direction -> scannedStates.get(entry.getKey().relative(direction)))
                    .filter(java.util.Objects::nonNull).toList();
            if (entry.getValue().satisfiesPlacementRules(neighbors)) {
                recordBuilder.aggregates().addCoolingPerTick(entry.getValue().coolingPerTick());
            }
        }
        recordBuilder.aggregates().efficiency(componentCount == 0 ? 0 : componentEfficiency / componentCount);
        StructureRecord record = recordBuilder.build(sectionRevisions);
        return new ValidationResult(ValidationStatus.VALID, "multiblock.validation.valid", null, null, null,
                footprint.cellCount(), footprint.cellCount(), null, record);
    }

    private ValidationResult invalid(String key, BlockPos position) {
        return new ValidationResult(ValidationStatus.INVALID, key, position, null, null,
                completedCells(), footprint.cellCount(), null, null);
    }

    private static boolean facesOutward(BlockState state, net.minecraft.core.Direction expected) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private static int componentIndex(int right, int up) {
        for (int i = 0; i < COMPONENT_OFFSETS.length; i++) {
            if (COMPONENT_OFFSETS[i][0] == right && COMPONENT_OFFSETS[i][1] == up) return i;
        }
        return -1;
    }

    private static ValidationFailure failure(String key, ResourceLocation expected, BlockState actual) {
        return new ValidationFailure(key, expected, BuiltInRegistries.BLOCK.getKey(actual.getBlock()));
    }

    private enum Kind { MAGNET, AMPLIFIER, FILLER }

    private record Component(Kind kind, String tier, double value) {
    }
}
