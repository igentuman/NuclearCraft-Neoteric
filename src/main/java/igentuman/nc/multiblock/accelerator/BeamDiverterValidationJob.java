package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.geometry.StructureTransform;
import igentuman.nc.multiblock.fusion.ElectromagnetDef;
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

public final class BeamDiverterValidationJob extends AcceleratorValidationJob {

    private static final int SIZE = 5;
    private static final int CENTER = 2;
    private static final int LAST = SIZE - 1;

    private static final ResourceLocation EXPECTED_CASING = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_SHELL = rl("accelerator_casing");
    private static final ResourceLocation EXPECTED_BEAM_PORT = rl("accelerator_beam_port");
    private static final ResourceLocation EXPECTED_BEAM = rl("particle_beam");
    private static final ResourceLocation EXPECTED_INNER = rl("electromagnet_yoke");
    private static final ResourceLocation EXPECTED_MAGNET = rl("electromagnet_yoke");

    private final BoxFootprint box;
    private final BeamDiverterValidationContext context;
    private final BlockPos[] beamPorts = new BlockPos[4];
    private ElectromagnetDef upMagnet;
    private ElectromagnetDef downMagnet;
    private int controllerCount;

    public BeamDiverterValidationJob(UUID structureId, BeamDiverterValidationContext context) {
        super(structureId, context.source());
        if (!(context.source().footprint() instanceof BoxFootprint footprint)) {
            throw new IllegalArgumentException("Beam diverter requires a box footprint");
        }
        if (footprint.width() != SIZE || footprint.height() != SIZE || footprint.depth() != SIZE) {
            throw new IllegalArgumentException("Beam diverter footprint must be a 5x5x5 box");
        }
        this.box = footprint;
        this.context = context;
        recordBuilder.aggregates()
                .heatCapacity(Math.multiplyExact(box.cellCount(), context.heatCapacityPerBlock()))
                .portChannels(4);
    }

    @Override
    protected ValidationFailure validateLoadedCell(StructureFootprint.Cell cell, BlockState state,
                                                     @Nullable BlockEntity blockEntity) {
        StructureTransform.LocalPosition local = box.transform().toLocal(cell.pos());
        int right = local.right();
        int up = local.up();
        int forward = local.forward();

        boolean boundaryRight = right == 0 || right == LAST;
        boolean boundaryUp = up == 0 || up == LAST;
        boolean boundaryForward = forward == 0 || forward == LAST;
        int boundaryAxes = (boundaryRight ? 1 : 0) + (boundaryUp ? 1 : 0) + (boundaryForward ? 1 : 0);

        if (boundaryAxes >= 2) {
            if (!context.cornerCasing().test(state)) {
                return failure("multiblock.diverter.wrong_corner", EXPECTED_CASING, state);
            }
            return null;
        }
        if (boundaryAxes == 1) {
            return validateShell(cell.pos(), state, right, up, forward);
        }
        return validateInner(cell.pos(), state, right, up, forward);
    }

    private ValidationFailure validateShell(BlockPos position, BlockState state, int right, int up, int forward) {
        int beamPortIndex = beamPortIndex(right, up, forward);
        if (beamPortIndex >= 0) {
            if (!context.beamPort().test(state)) return failure("multiblock.diverter.missing_beam_port", EXPECTED_BEAM_PORT, state);
            Direction expected = switch (beamPortIndex) {
                case 0 -> box.transform().forward().getOpposite();
                case 1 -> box.transform().right();
                case 2 -> box.transform().forward();
                default -> box.transform().right().getOpposite();
            };
            if (!facesOutward(state, expected)) return failure("multiblock.diverter.beam_port_facing", EXPECTED_BEAM_PORT, state);
            beamPorts[beamPortIndex] = position.immutable();
            return null;
        }
        if (context.beamPort().test(state)) return failure("multiblock.diverter.unexpected_beam_port", EXPECTED_SHELL, state);
        if (context.controller().test(state)) {
            if (!position.equals(source.controllerPos()) || !facesOutward(state, source.orientation())) {
                return failure("multiblock.diverter.controller_position", rl("beam_diverter_controller"), state);
            }
            controllerCount++;
            return null;
        }
        if (context.servicePort().test(state)) {
            recordBuilder.role(StructureRole.SERVICE_PORT, position);
            return null;
        }
        if (!context.shell().test(state)) return failure("multiblock.diverter.wrong_shell", EXPECTED_SHELL, state);
        return null;
    }

    private ValidationFailure validateInner(BlockPos position, BlockState state, int right, int up, int forward) {
        boolean centerColumn = right == CENTER && forward == CENTER;
        if (centerColumn && up == CENTER) {
            if (!context.particleBeam().test(state)) return failure("multiblock.diverter.wrong_beam", EXPECTED_BEAM, state);
            recordBuilder.role(StructureRole.PARTICLE_BEAM, position);
            return null;
        }
        boolean horizontalBeamNeighbor = up == CENTER
                && ((right == CENTER && (forward == CENTER - 1 || forward == CENTER + 1))
                || (forward == CENTER && (right == CENTER - 1 || right == CENTER + 1)));
        if (horizontalBeamNeighbor) {
            if (!context.particleBeam().test(state)) return failure("multiblock.diverter.wrong_beam", EXPECTED_BEAM, state);
            recordBuilder.role(StructureRole.PARTICLE_BEAM, position);
            return null;
        }
        if (centerColumn && (up == CENTER - 1 || up == CENTER + 1)) {
            ElectromagnetDef magnet = context.electromagnet().apply(state);
            if (magnet == null) return failure("multiblock.diverter.wrong_magnet", EXPECTED_MAGNET, state);
            recordBuilder.role(StructureRole.ELECTROMAGNET, position);
            recordBuilder.aggregates().addEnergyPerTick(magnet.power).addHeatPerTick(magnet.heat)
                    .addDipoleField(magnet.magneticField / 2D)
                    .includeMaximumTemperatureK(magnet.maxTemp / 1_000L);
            if (up == CENTER - 1) {
                downMagnet = magnet;
            } else {
                upMagnet = magnet;
            }
            if (upMagnet != null && downMagnet != null && !upMagnet.name.equals(downMagnet.name)) {
                return new ValidationFailure("multiblock.diverter.mixed_magnet_tier", EXPECTED_MAGNET, EXPECTED_MAGNET);
            }
            return null;
        }
        if (!state.isAir() && !context.yoke().test(state)) {
            return failure("multiblock.diverter.wrong_inner", EXPECTED_INNER, state);
        }
        return null;
    }

    private static int beamPortIndex(int right, int up, int forward) {
        if (up != CENTER) return -1;
        if (right == CENTER && forward == 0) return 0;
        if (right == LAST && forward == CENTER) return 1;
        if (right == CENTER && forward == LAST) return 2;
        if (right == 0 && forward == CENTER) return 3;
        return -1;
    }

    @Override
    protected ValidationResult completedResult(Map<Long, Long> sectionRevisions) {
        if (controllerCount != 1) return invalid("multiblock.diverter.controller_count", source.controllerPos());
        for (BlockPos beamPort : beamPorts) {
            if (beamPort == null) return invalid("multiblock.diverter.beam_port_count", source.controllerPos());
        }
        if (upMagnet == null || downMagnet == null) {
            return invalid("multiblock.diverter.incomplete_magnet_pair", source.controllerPos());
        }
        recordBuilder.role(StructureRole.BEAM_INPUT, beamPorts[0]);
        for (int i = 1; i < beamPorts.length; i++) recordBuilder.role(StructureRole.BEAM_OUTPUT, beamPorts[i]);
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

    private static ValidationFailure failure(String key, ResourceLocation expected, BlockState actual) {
        return new ValidationFailure(key, expected, BuiltInRegistries.BLOCK.getKey(actual.getBlock()));
    }
}
