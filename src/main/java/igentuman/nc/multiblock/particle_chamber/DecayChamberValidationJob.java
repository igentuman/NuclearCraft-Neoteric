package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.IBeamPort;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.geometry.StructureTransform;
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

import java.util.Map;
import java.util.UUID;

import static igentuman.nc.NuclearCraft.rl;

public final class DecayChamberValidationJob extends ParticleChamberValidationJob {

    private static final ResourceLocation EXPECTED_CASING = rl("target_chamber_casing");
    private static final ResourceLocation EXPECTED_SHELL = rl("target_chamber_casing");
    private static final ResourceLocation EXPECTED_BEAM_PORT = rl("target_chamber_beam_port");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_CAMERA = rl("target_chamber_camera");
    private static final ResourceLocation EXPECTED_INNER = rl("target_chamber_inner");
    private static final ResourceLocation EXPECTED_CONTROLLER = rl("decay_chamber_controller");

    private final BoxFootprint box;
    private final DecayChamberValidationContext context;
    private final BlockPos[] beamPorts = new BlockPos[4];
    private final BeamPortMode[] beamModes = new BeamPortMode[4];
    private int controllerCount;
    private int cameraCount;
    private int validDetectorCount;
    private int allDetectorCount;
    private double detectorEfficiency;

    public DecayChamberValidationJob(UUID structureId, DecayChamberValidationContext context) {
        super(structureId, context.source());
        if (!(context.source().footprint() instanceof BoxFootprint footprint)) {
            throw new IllegalArgumentException("Decay chamber requires a box footprint");
        }
        if (footprint.width() != footprint.height() || footprint.width() != footprint.depth()
                || (footprint.width() & 1) == 0) {
            throw new IllegalArgumentException("Decay chamber footprint must be an odd cube");
        }
        this.box = footprint;
        this.context = context;
        recordBuilder.aggregates().portChannels(4);
    }

    @Override
    protected ValidationFailure validateLoadedCell(StructureFootprint.Cell cell, BlockState state,
                                                     @Nullable BlockEntity blockEntity) {
        StructureTransform.LocalPosition local = box.transform().toLocal(cell.pos());
        int right = local.right();
        int up = local.up();
        int forward = local.forward();
        int last = box.width() - 1;
        int center = box.width() / 2;
        boolean boundary = right == 0 || right == last || up == 0 || up == last
                || forward == 0 || forward == last;
        int beamPortIndex = beamPortIndex(right, up, forward, center, last);

        if (beamPortIndex >= 0) return validateBeamPort(cell.pos(), state, beamPortIndex, blockEntity);
        if (boundary) return validateShell(cell.pos(), state, right, up, forward, last);
        if (right == center && up == center && forward == center) {
            if (!context.camera().test(state)) return failure("multiblock.decay.wrong_camera", EXPECTED_CAMERA, state);
            cameraCount++;
            recordBuilder.role(StructureRole.CAMERA, cell.pos());
            return null;
        }
        if (up == center && (right == center || forward == center)) {
            if (!context.particleBeam().test(state)) return failure("multiblock.decay.wrong_beam", EXPECTED_BEAM, state);
            recordBuilder.role(StructureRole.PARTICLE_BEAM, cell.pos());
            return null;
        }
        if (context.camera().test(state)) return failure("multiblock.decay.extra_camera", EXPECTED_INNER, state);
        DetectorDef detector = context.detector().apply(state);
        if (detector != null) {
            allDetectorCount++;
            if (manhattan(cell.pos(), centerPosition()) <= detector.maximumDistance()) {
                validDetectorCount++;
                detectorEfficiency += detector.efficiency();
                recordBuilder.role(StructureRole.DETECTOR, cell.pos());
                recordBuilder.aggregates().detector(detector.id()).addEnergyPerTick(detector.energyPerTick());
            }
            return null;
        }
        if (!state.isAir() && !context.innerFill().test(state)) {
            return failure("multiblock.decay.wrong_inner", EXPECTED_INNER, state);
        }
        return null;
    }

    private ValidationFailure validateBeamPort(BlockPos position, BlockState state, int index,
                                                @Nullable BlockEntity blockEntity) {
        if (!context.beamPort().test(state)) return failure("multiblock.decay.missing_beam_port", EXPECTED_BEAM_PORT, state);
        Direction expected = switch (index) {
            case 0 -> box.transform().forward().getOpposite();
            case 1 -> box.transform().right();
            case 2 -> box.transform().forward();
            default -> box.transform().right().getOpposite();
        };
        if (!facesOutward(state, expected)) return failure("multiblock.decay.beam_port_facing", EXPECTED_BEAM_PORT, state);
        beamPorts[index] = position.immutable();
        beamModes[index] = blockEntity instanceof IBeamPort port ? port.beamPortMode() : BeamPortMode.DISABLED;
        return null;
    }

    @Override
    protected boolean requiresBlockEntity(StructureFootprint.Cell cell, BlockState state) {
        StructureTransform.LocalPosition local = box.transform().toLocal(cell.pos());
        int center = box.width() / 2;
        return beamPortIndex(local.right(), local.up(), local.forward(), center, box.width() - 1) >= 0;
    }

    private ValidationFailure validateShell(BlockPos position, BlockState state, int right, int up,
                                             int forward, int last) {
        int boundaryAxes = (right == 0 || right == last ? 1 : 0)
                + (up == 0 || up == last ? 1 : 0)
                + (forward == 0 || forward == last ? 1 : 0);
        if (boundaryAxes >= 2 && !context.cornerCasing().test(state)) {
            return failure("multiblock.decay.wrong_corner", EXPECTED_CASING, state);
        }
        if (!context.shell().test(state)) return failure("multiblock.decay.wrong_shell", EXPECTED_SHELL, state);
        if (context.beamPort().test(state)) return failure("multiblock.decay.unexpected_beam_port", EXPECTED_SHELL, state);
        if (context.controller().test(state)) {
            if (!position.equals(source.controllerPos()) || !facesOutward(state, source.orientation())) {
                return failure("multiblock.decay.controller_position", EXPECTED_CONTROLLER, state);
            }
            controllerCount++;
        } else if (context.servicePort().test(state)) {
            recordBuilder.role(StructureRole.SERVICE_PORT, position);
        }
        return null;
    }

    @Override
    protected ValidationResult completedResult(Map<Long, Long> sectionRevisions) {
        if (controllerCount != 1) return invalid("multiblock.decay.controller_count", source.controllerPos());
        if (cameraCount != 1) return invalid("multiblock.decay.camera_count", centerPosition());
        for (BlockPos beamPort : beamPorts) {
            if (beamPort == null) return invalid("multiblock.decay.beam_port_count", centerPosition());
        }
        if (validDetectorCount == 0) return invalid("multiblock.decay.no_valid_detectors", centerPosition());
        for (int i = 0; i < beamPorts.length; i++) {
            if (beamModes[i] == BeamPortMode.INPUT) recordBuilder.role(StructureRole.BEAM_INPUT, beamPorts[i]);
            else if (beamModes[i] == BeamPortMode.OUTPUT) recordBuilder.role(StructureRole.BEAM_OUTPUT, beamPorts[i]);
        }
        recordBuilder.aggregates().efficiency(1D + detectorEfficiency);
        recordBuilder.aggregates().addEnergyPerTick(Multiblocks.decayChamberBasePower);
        StructureRecord record = recordBuilder.build(sectionRevisions);
        return new ValidationResult(ValidationStatus.VALID, "multiblock.validation.valid", null, null, null,
                footprint.cellCount(), footprint.cellCount(), null, record);
    }

    private BlockPos centerPosition() {
        int center = box.width() / 2;
        return box.transform().toWorld(center, center, center);
    }

    private static int beamPortIndex(int right, int up, int forward, int center, int last) {
        if (up != center) return -1;
        if (right == center && forward == 0) return 0;
        if (right == last && forward == center) return 1;
        if (right == center && forward == last) return 2;
        if (right == 0 && forward == center) return 3;
        return -1;
    }

    private ValidationResult invalid(String key, BlockPos position) {
        return new ValidationResult(ValidationStatus.INVALID, key, position, null, null,
                completedCells(), footprint.cellCount(), null, null);
    }

    private static boolean facesOutward(BlockState state, Direction expected) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private static int manhattan(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY())
                + Math.abs(first.getZ() - second.getZ());
    }

    private static ValidationFailure failure(String key, ResourceLocation expected, BlockState actual) {
        return new ValidationFailure(key, expected, BuiltInRegistries.BLOCK.getKey(actual.getBlock()));
    }
}
