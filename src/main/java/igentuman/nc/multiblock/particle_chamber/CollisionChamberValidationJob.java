package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.multiblock.StructureFootprint;
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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import static igentuman.nc.NuclearCraft.rl;

public final class CollisionChamberValidationJob extends ParticleChamberValidationJob {

    private static final ResourceLocation EXPECTED_CASING = rl("target_chamber_casing");
    private static final ResourceLocation EXPECTED_SHELL = rl("target_chamber_casing");
    private static final ResourceLocation EXPECTED_BEAM_PORT = rl("target_chamber_beam_port");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_CAMERA = rl("target_chamber_camera");
    private static final ResourceLocation EXPECTED_INNER = rl("target_chamber_inner");
    private static final ResourceLocation EXPECTED_CONTROLLER = rl("collision_chamber_controller");

    private final BoxFootprint box;
    private final CollisionChamberValidationContext context;
    private final BlockPos[] inputPorts = new BlockPos[2];
    private final Map<Integer, BlockPos[]> outputPortsByForward = new TreeMap<>();
    private final TreeSet<Integer> cameraForwards = new TreeSet<>();
    private final Set<BlockPos> beamPositions = new HashSet<>();
    private int controllerCount;
    private int validDetectorCount;
    private int allDetectorCount;
    private double detectorEfficiency;

    public CollisionChamberValidationJob(UUID structureId, CollisionChamberValidationContext context) {
        super(structureId, context.source());
        if (!(context.source().footprint() instanceof BoxFootprint footprint)) {
            throw new IllegalArgumentException("Collision chamber requires a box footprint");
        }
        if (footprint.width() != footprint.height()) {
            throw new IllegalArgumentException("Collision chamber requires equal transverse dimensions");
        }
        this.box = footprint;
        this.context = context;
        recordBuilder.aggregates().portChannels(6);
    }

    @Override
    protected ValidationFailure validateLoadedCell(StructureFootprint.Cell cell, BlockState state,
                                                     @Nullable BlockEntity blockEntity) {
        StructureTransform.LocalPosition local = box.transform().toLocal(cell.pos());
        int right = local.right();
        int up = local.up();
        int forward = local.forward();
        int rLast = box.width() - 1;
        int uLast = box.height() - 1;
        int fLast = box.depth() - 1;
        int rCenter = box.width() / 2;
        int uCenter = box.height() / 2;
        boolean boundary = right == 0 || right == rLast || up == 0 || up == uLast
                || forward == 0 || forward == fLast;

        if (right == rCenter && up == uCenter && (forward == 0 || forward == fLast)) {
            return validateInputPort(cell.pos(), state, forward == 0 ? 0 : 1);
        }
        boolean outputWallCell = (right == 0 || right == rLast) && up == uCenter && forward > 0 && forward < fLast;
        if (outputWallCell && context.beamPort().test(state)) {
            return validateOutputPort(cell.pos(), state, right == 0, forward);
        }
        if (boundary) return validateShell(cell.pos(), state, right, up, forward, rLast, uLast, fLast);
        if (up == uCenter) {
            if (right == rCenter) {
                if (context.camera().test(state)) {
                    cameraForwards.add(forward);
                    recordBuilder.role(StructureRole.CAMERA, cell.pos());
                    return null;
                }
                if (!context.particleBeam().test(state)) return failure("multiblock.collision.wrong_beam", EXPECTED_BEAM, state);
                recordBuilder.role(StructureRole.PARTICLE_BEAM, cell.pos());
                return null;
            }
        }
        if (context.particleBeam().test(state)) {
            beamPositions.add(cell.pos().immutable());
            recordBuilder.role(StructureRole.PARTICLE_BEAM, cell.pos());
            return null;
        }
        DetectorDef detector = context.detector().apply(state);
        if (detector != null) {
            allDetectorCount++;
            if (axisDistance(right, up, rCenter, uCenter) <= detector.maximumDistance()) {
                validDetectorCount++;
                detectorEfficiency += detector.efficiency();
                recordBuilder.role(StructureRole.DETECTOR, cell.pos());
                recordBuilder.aggregates().detector(detector.id()).addEnergyPerTick(detector.energyPerTick());
            }
            return null;
        }
        if (!state.isAir() && !context.innerFill().test(state)) {
            return failure("multiblock.collision.wrong_inner", EXPECTED_INNER, state);
        }
        return null;
    }

    private ValidationFailure validateInputPort(BlockPos position, BlockState state, int index) {
        if (!context.beamPort().test(state)) return failure("multiblock.collision.missing_input_port", EXPECTED_BEAM_PORT, state);
        Direction expected = index == 0 ? box.transform().forward().getOpposite() : box.transform().forward();
        if (!facesOutward(state, expected)) return failure("multiblock.collision.input_port_facing", EXPECTED_BEAM_PORT, state);
        inputPorts[index] = position.immutable();
        return null;
    }

    private ValidationFailure validateOutputPort(BlockPos position, BlockState state, boolean nearSide, int forward) {
        Direction expected = nearSide ? box.transform().right().getOpposite() : box.transform().right();
        if (!facesOutward(state, expected)) return failure("multiblock.collision.output_port_facing", EXPECTED_BEAM_PORT, state);
        BlockPos[] pair = outputPortsByForward.computeIfAbsent(forward, k -> new BlockPos[2]);
        pair[nearSide ? 0 : 1] = position.immutable();
        return null;
    }

    private ValidationFailure validateShell(BlockPos position, BlockState state, int right, int up, int forward,
                                             int rLast, int uLast, int fLast) {
        int boundaryAxes = (right == 0 || right == rLast ? 1 : 0)
                + (up == 0 || up == uLast ? 1 : 0)
                + (forward == 0 || forward == fLast ? 1 : 0);
        if (boundaryAxes >= 2 && !context.cornerCasing().test(state)) {
            return failure("multiblock.collision.wrong_corner", EXPECTED_CASING, state);
        }
        if (!context.shell().test(state)) return failure("multiblock.collision.wrong_shell", EXPECTED_SHELL, state);
        if (context.beamPort().test(state)) return failure("multiblock.collision.unexpected_beam_port", EXPECTED_SHELL, state);
        if (context.controller().test(state)) {
            if (!position.equals(source.controllerPos()) || !facesOutward(state, source.orientation())) {
                return failure("multiblock.collision.controller_position", EXPECTED_CONTROLLER, state);
            }
            controllerCount++;
        } else if (context.servicePort().test(state)) {
            recordBuilder.role(StructureRole.SERVICE_PORT, position);
        }
        return null;
    }

    @Override
    protected ValidationResult completedResult(Map<Long, Long> sectionRevisions) {
        if (controllerCount != 1) return invalid("multiblock.collision.controller_count", source.controllerPos());
        if (inputPorts[0] == null || inputPorts[1] == null) {
            return invalid("multiblock.collision.input_port_count", source.controllerPos());
        }
        if (cameraForwards.size() < 2) return invalid("multiblock.collision.camera_count", source.controllerPos());
        int[] outputsPerSide = new int[2];
        for (Map.Entry<Integer, BlockPos[]> entry : outputPortsByForward.entrySet()) {
            BlockPos[] pair = entry.getValue();
            for (int side = 0; side < pair.length; side++) {
                BlockPos port = pair[side];
                if (port == null) continue;
                outputsPerSide[side]++;
                if (!cameraForwards.contains(entry.getKey())) {
                    return invalid("multiblock.collision.output_port_no_camera", port);
                }
                int step = side == 0 ? 1 : -1;
                int start = side == 0 ? 1 : box.width() - 2;
                for (int right = start; right != box.width() / 2; right += step) {
                    BlockPos beam = box.transform().toWorld(right, box.height() / 2, entry.getKey());
                    if (!beamPositions.contains(beam)) {
                        return invalid("multiblock.collision.wrong_corridor", beam);
                    }
                }
            }
        }
        if (outputsPerSide[0] != 2 || outputsPerSide[1] != 2) {
            return invalid("multiblock.collision.output_port_count", source.controllerPos());
        }

        recordBuilder.role(StructureRole.BEAM_INPUT, inputPorts[0]);
        recordBuilder.role(StructureRole.BEAM_INPUT, inputPorts[1]);
        for (BlockPos[] pair : outputPortsByForward.values()) {
            for (BlockPos port : pair) {
                if (port != null) recordBuilder.role(StructureRole.BEAM_OUTPUT, port);
            }
        }
        recordBuilder.aggregates().efficiency(1D + detectorEfficiency);
        recordBuilder.aggregates().addEnergyPerTick(Multiblocks.collisionChamberBasePower);
        StructureRecord record = recordBuilder.build(sectionRevisions);
        return new ValidationResult(ValidationStatus.VALID, "multiblock.validation.valid", null, null, null,
                footprint.cellCount(), footprint.cellCount(), null, record);
    }

    private static int axisDistance(int right, int up, int rCenter, int uCenter) {
        return Math.abs(right - rCenter) + Math.abs(up - uCenter);
    }

    private ValidationResult invalid(String key, BlockPos position) {
        return new ValidationResult(ValidationStatus.INVALID, key, position, null, null,
                completedCells(), footprint.cellCount(), null, null);
    }

    private static boolean facesOutward(BlockState state, Direction expected) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    private static ValidationFailure failure(String key, ResourceLocation expected, BlockState actual) {
        return new ValidationFailure(key, expected, BuiltInRegistries.BLOCK.getKey(actual.getBlock()));
    }
}
