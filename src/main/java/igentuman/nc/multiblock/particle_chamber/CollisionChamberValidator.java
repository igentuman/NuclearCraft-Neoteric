package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.config.Multiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollisionChamberValidator extends AbstractChamberValidator {

    private final BlockPos[] inputPorts = new BlockPos[2];
    private final Map<Integer, BlockPos[]> outputPortsByForward = new TreeMap<>();
    private final TreeSet<Integer> cameraForwards = new TreeSet<>();
    private final Set<BlockPos> beamPositions = new HashSet<>();
    private int rightCenter;
    private int upCenter;

    public CollisionChamberValidator() {
        super("collision", "collision_chamber_controller");
    }

    private static int minimumTransverse() {
        return Math.min(Multiblocks.collisionChamberMinTransverseSize, Multiblocks.collisionChamberMaxTransverseSize);
    }

    private static int maximumTransverse() {
        return Math.max(Multiblocks.collisionChamberMinTransverseSize, Multiblocks.collisionChamberMaxTransverseSize);
    }

    private static int minimumLength() {
        return Math.min(Multiblocks.collisionChamberMinLength, Multiblocks.collisionChamberMaxLength);
    }

    private static int maximumLength() {
        return Math.max(Multiblocks.collisionChamberMinLength, Multiblocks.collisionChamberMaxLength);
    }

    @Override
    protected int maximumHeight() {
        return maximumTransverse();
    }

    @Override
    protected int maximumLateral() {
        return Math.max(maximumTransverse(), maximumLength());
    }

    @Override
    protected int maximumDepth() {
        return maximumLength();
    }

    @Override
    protected void resetPass() {
        inputPorts[0] = null;
        inputPorts[1] = null;
        outputPortsByForward.clear();
        cameraForwards.clear();
        beamPositions.clear();
        rightCenter = 0;
        upCenter = 0;
    }

    @Override
    protected long basePower() {
        return Multiblocks.collisionChamberBasePower;
    }

    @Override
    protected boolean orient(BlockPos frontBottomLeft, Direction inward, Direction wallRight,
                             int measuredWidth, int measuredHeight, int measuredDepth) {
        boolean sideWall = measuredDepth > 4 && measuredDepth < minimumLength();
        int transverse = sideWall ? measuredDepth : measuredWidth;
        int length = sideWall ? measuredWidth : measuredDepth;
        if (transverse < minimumTransverse() || transverse > maximumTransverse()
                || measuredHeight < minimumTransverse() || measuredHeight > maximumTransverse()
                || length < minimumLength() || length > maximumLength()) {
            return wrongSize("multiblock.discovery.wrong_size");
        }
        if (transverse != measuredHeight) return wrongSize("multiblock.discovery.expected_collision_box");
        if (sideWall) frame(frontBottomLeft, inward, wallRight, transverse, measuredHeight, length);
        else frame(frontBottomLeft, wallRight, inward, transverse, measuredHeight, length);
        rightCenter = transverse / 2;
        upCenter = measuredHeight / 2;
        return true;
    }

    @Override
    protected boolean validateShellCell(ParticleChamberCache cache, BlockPos pos, BlockState state,
                                        int r, int u, int f) {
        int last = depth - 1;
        if (r == rightCenter && u == upCenter && (f == 0 || f == last)) {
            int index = f == 0 ? 0 : 1;
            if (!state.is(beamPort)) return invalid("missing_input_port", pos, EXPECTED_BEAM_PORT, state);
            if (!faces(state, index == 0 ? forward.getOpposite() : forward)) {
                return invalid("input_port_facing", pos, EXPECTED_BEAM_PORT, state);
            }
            inputPorts[index] = pos.immutable();
            cache.addWorkingPort(pos);
            return true;
        }
        boolean outputWall = (r == 0 || r == width - 1) && u == upCenter && f > 0 && f < last;
        if (outputWall && state.is(beamPort)) {
            boolean nearSide = r == 0;
            if (!faces(state, nearSide ? right.getOpposite() : right)) {
                return invalid("output_port_facing", pos, EXPECTED_BEAM_PORT, state);
            }
            outputPortsByForward.computeIfAbsent(f, ignored -> new BlockPos[2])[nearSide ? 0 : 1] = pos.immutable();
            cache.addWorkingPort(pos);
            return true;
        }
        return super.validateShellCell(cache, pos, state, r, u, f);
    }

    @Override
    protected boolean validateInnerCell(ParticleChamberCache cache, BlockPos pos, BlockState state,
                                        int r, int u, int f) {
        if (u == upCenter && r == rightCenter) {
            if (state.is(camera)) {
                cameraForwards.add(f);
                return true;
            }
            if (!state.is(particleBeam)) return invalid("wrong_beam", pos, EXPECTED_BEAM, state);
            return true;
        }
        if (state.is(particleBeam)) {
            beamPositions.add(pos.immutable());
            return true;
        }
        if (countDetector(cache, state, Math.abs(r - rightCenter) + Math.abs(u - upCenter))) return true;
        return validateFill(pos, state);
    }

    @Override
    protected boolean validateLayout(ParticleChamberCache cache) {
        BlockPos controllerPos = cache.controllerPos();
        if (inputPorts[0] == null || inputPorts[1] == null) return invalid("input_port_count", controllerPos);
        if (cameraForwards.size() < 2) return invalid("camera_count", controllerPos);
        int[] outputsPerSide = new int[2];
        for (Map.Entry<Integer, BlockPos[]> entry : outputPortsByForward.entrySet()) {
            BlockPos[] pair = entry.getValue();
            for (int side = 0; side < pair.length; side++) {
                BlockPos port = pair[side];
                if (port == null) continue;
                outputsPerSide[side]++;
                if (!cameraForwards.contains(entry.getKey())) return invalid("output_port_no_camera", port);
                int step = side == 0 ? 1 : -1;
                for (int r = side == 0 ? 1 : width - 2; r != rightCenter; r += step) {
                    BlockPos beam = at(r, upCenter, entry.getKey());
                    if (!beamPositions.contains(beam)) return invalid("wrong_corridor", beam);
                }
            }
        }
        if (outputsPerSide[0] != 2 || outputsPerSide[1] != 2) return invalid("output_port_count", controllerPos);
        cache.addBeamInput(inputPorts[0]);
        cache.addBeamInput(inputPorts[1]);
        for (BlockPos[] pair : outputPortsByForward.values()) {
            for (BlockPos port : pair) {
                if (port != null) cache.addBeamOutput(port);
            }
        }
        return true;
    }
}
