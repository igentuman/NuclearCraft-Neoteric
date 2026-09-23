package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block.particle.ParticleChamberBeamPortBlock;
import igentuman.nc.config.Multiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public class CubeChamberValidator extends AbstractChamberValidator {

    private final IntSupplier minimumSize;
    private final IntSupplier maximumSize;
    private final LongSupplier basePower;
    private final BlockPos[] beamPorts = new BlockPos[4];
    private final BeamPortMode[] beamModes = new BeamPortMode[4];
    private int center;
    private int cameraCount;
    private BlockPos centerPos;

    public CubeChamberValidator(String machine, String controllerName, IntSupplier minimumSize,
                                IntSupplier maximumSize, LongSupplier basePower) {
        super(machine, controllerName);
        this.minimumSize = minimumSize;
        this.maximumSize = maximumSize;
        this.basePower = basePower;
    }

    public static CubeChamberValidator target() {
        return new CubeChamberValidator("target", "target_chamber_controller",
                () -> Multiblocks.targetChamberMinSize, () -> Multiblocks.targetChamberMaxSize, () -> 0L);
    }

    public static CubeChamberValidator decay() {
        return new CubeChamberValidator("decay", "decay_chamber_controller",
                () -> Multiblocks.decayChamberMinSize, () -> Multiblocks.decayChamberMaxSize,
                () -> Multiblocks.decayChamberBasePower);
    }

    private int minimum() {
        return Math.min(minimumSize.getAsInt(), maximumSize.getAsInt());
    }

    private int maximum() {
        return Math.max(minimumSize.getAsInt(), maximumSize.getAsInt());
    }

    @Override
    protected int maximumHeight() {
        return maximum();
    }

    @Override
    protected int maximumLateral() {
        return maximum();
    }

    @Override
    protected int maximumDepth() {
        return maximum();
    }

    @Override
    protected void resetPass() {
        for (int index = 0; index < beamPorts.length; index++) {
            beamPorts[index] = null;
            beamModes[index] = BeamPortMode.DISABLED;
        }
        center = 0;
        cameraCount = 0;
        centerPos = null;
    }

    @Override
    protected long basePower() {
        return basePower.getAsLong();
    }

    @Override
    protected boolean orient(BlockPos frontBottomLeft, Direction inward, Direction wallRight,
                             int measuredWidth, int measuredHeight, int measuredDepth) {
        int min = minimum();
        int max = maximum();
        if (measuredWidth < min || measuredWidth > max || measuredHeight < min || measuredHeight > max
                || measuredDepth < min || measuredDepth > max) {
            return wrongSize("multiblock.discovery.wrong_size");
        }
        if (measuredWidth != measuredHeight || measuredHeight != measuredDepth || (measuredWidth & 1) == 0) {
            return wrongSize("multiblock.discovery.expected_odd_cube");
        }
        frame(frontBottomLeft, wallRight, inward, measuredWidth, measuredHeight, measuredDepth);
        center = measuredWidth / 2;
        centerPos = at(center, center, center);
        return true;
    }

    @Override
    protected boolean validateShellCell(ParticleChamberCache cache, BlockPos pos, BlockState state,
                                        int r, int u, int f) {
        int index = beamPortIndex(r, u, f);
        if (index < 0) return super.validateShellCell(cache, pos, state, r, u, f);
        if (!state.is(beamPort)) return invalid("missing_beam_port", pos, EXPECTED_BEAM_PORT, state);
        Direction expected = switch (index) {
            case 0 -> forward.getOpposite();
            case 1 -> right;
            case 2 -> forward;
            default -> right.getOpposite();
        };
        if (!faces(state, expected)) return invalid("beam_port_facing", pos, EXPECTED_BEAM_PORT, state);
        beamPorts[index] = pos.immutable();
        beamModes[index] = state.hasProperty(ParticleChamberBeamPortBlock.PORT_MODE)
                ? state.getValue(ParticleChamberBeamPortBlock.PORT_MODE) : BeamPortMode.DISABLED;
        cache.addWorkingPort(pos);
        return true;
    }

    @Override
    protected boolean validateInnerCell(ParticleChamberCache cache, BlockPos pos, BlockState state,
                                        int r, int u, int f) {
        if (r == center && u == center && f == center) {
            if (!state.is(camera)) return invalid("wrong_camera", pos, EXPECTED_CAMERA, state);
            cameraCount++;
            return true;
        }
        if (u == center && (r == center || f == center)) {
            if (!state.is(particleBeam)) return invalid("wrong_beam", pos, EXPECTED_BEAM, state);
            return true;
        }
        if (state.is(camera)) return invalid("extra_camera", pos, EXPECTED_INNER, state);
        if (countDetector(cache, state, manhattan(pos, centerPos))) return true;
        return validateFill(pos, state);
    }

    @Override
    protected boolean validateLayout(ParticleChamberCache cache) {
        if (cameraCount != 1) return invalid("camera_count", centerPos);
        for (BlockPos port : beamPorts) {
            if (port == null) return invalid("beam_port_count", centerPos);
        }
        if (cache.workingValidDetectors() == 0) return invalid("no_valid_detectors", centerPos);
        for (int index = 0; index < beamPorts.length; index++) {
            if (beamModes[index] == BeamPortMode.INPUT) cache.addBeamInput(beamPorts[index]);
            else if (beamModes[index] == BeamPortMode.OUTPUT) cache.addBeamOutput(beamPorts[index]);
        }
        return true;
    }

    private int beamPortIndex(int r, int u, int f) {
        int last = width - 1;
        if (u != center) return -1;
        if (r == center && f == 0) return 0;
        if (r == last && f == center) return 1;
        if (r == center && f == last) return 2;
        if (r == 0 && f == center) return 3;
        return -1;
    }

    private static int manhattan(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY())
                + Math.abs(first.getZ() - second.getZ());
    }
}
