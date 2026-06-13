package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.COLLISION_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class CollisionChamberMultiblock extends ParticleChamberMultiblock {

    public static final int REQUIRED_INPUTS = 2;
    public static final int REQUIRED_CAMERAS = 2;
    public static final int REQUIRED_OUTPUTS = 4;
    public static final int REQUIRED_OUTPUTS_PER_SIDE = 2;

    public final List<BlockPos> inputPorts = new ArrayList<>(REQUIRED_INPUTS);
    public final List<BlockPos> outputPorts = new ArrayList<>(REQUIRED_OUTPUTS);

    @Override public int maxHeight() { return 11; }
    @Override public int maxWidth()  { return 11; }
    @Override public int maxDepth()  { return COLLISION_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int minHeight() { return 5; }
    @Override public int minWidth()  { return 5; }
    @Override public int minDepth()  { return COLLISION_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    protected boolean requireCubeShape() {
        // collision chambers allow rectangular ratios in addition to cubes
        return false;
    }

    public CollisionChamberMultiblock(CollisionChamberControllerBE controllerBE) {
        super(
                getBlocksByTagKey(COLLISION_CHAMBER_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(TARGET_CHAMBER_INNER_BLOCKS.location().toString()),
                new ParticleChamberController(controllerBE)
        );
        id = "collision_chamber_" + controllerBE.getBlockPos().toShortString();
        controllerBe = controllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public void validateOuter() {
        super.validateOuter();
        if (!validationResult.isValid) {
            return;
        }
        // odd width/height are required so the beam line sits on the chamber centre
        if (width % 2 == 0 || height % 2 == 0) {
            debugLog("Collision chamber requires odd width and height, got " + width + "x" + height);
            validationResult = ValidationResult.WRONG_PROPORTIONS;
            outerValid = false;
        }
    }

    @Override
    public void validateInner() {
        debugLog("Collision chamber inner validation");
        if (!outerValid) {
            clearStats();
            return;
        }
        inputPorts.clear();
        outputPorts.clear();
        beamPorts.clear();

        indexInnerBlocks();
        if (!validationResult.isValid) {
            clearStats();
            return;
        }

        if (!validateBeamAxis()) {
            clearStats();
            return;
        }
        if (!validateOutputPorts()) {
            clearStats();
            return;
        }

        CollisionChamberControllerBE ctrl = (CollisionChamberControllerBE) controllerBE();
        ctrl.connectedPorts = inputPorts.size() + outputPorts.size();
        ctrl.efficiency = 1D;
        ctrl.height = height;
        ctrl.width = width;
        ctrl.depth = depth;
        validationResult = ValidationResult.VALID;
        innerValid = true;
    }

    /**
     * Walks the beam line along the depth (controller-facing) axis through the chamber centre.
     * The {@code depth - 2} interior slots must be particle beam blocks carrying at least
     * {@link #REQUIRED_CAMERAS} chamber cameras; both end caps must be beam ports in input mode.
     */
    private boolean validateBeamAxis() {
        int xc = width / 2;
        int yc = height / 2;
        Direction dir = getControllerDirection();
        Block beam = ACCELERATOR_BLOCKS.get("particle_beam").get();
        Block camera = PARTICLE_CHAMBER_BLOCKS.get("target_chamber_camera").get();

        int cameras = 0;
        for (int z = 1; z < depth - 1; z++) {
            BlockPos pos = getSidePos(xc - leftCasing).above(yc - bottomCasing).relative(dir, -z);
            BlockState bs = getBlockState(pos);
            if (bs.is(camera)) {
                cameras++;
            } else if (!bs.is(beam)) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = new BlockPos(pos);
                debugLog("Collision beam line block at " + pos.toShortString() + " must be particle beam or chamber camera");
                return false;
            }
        }
        if (cameras < REQUIRED_CAMERAS) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = new BlockPos(getSidePos(xc - leftCasing).above(yc - bottomCasing).relative(dir, -(depth / 2)));
            debugLog("Collision beam line needs at least " + REQUIRED_CAMERAS + " chamber cameras, found " + cameras);
            return false;
        }

        BlockPos near = new BlockPos(getSidePos(xc - leftCasing).above(yc - bottomCasing));
        BlockPos far = new BlockPos(getSidePos(xc - leftCasing).above(yc - bottomCasing).relative(dir, -(depth - 1)));
        for (BlockPos end : List.of(near, far)) {
            if (getBlockEntity(end) instanceof TargetChamberBeamPortBE port && port.isInput()) {
                beamPorts.put(end.asLong(), port);
                inputPorts.add(end);
            } else {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = end;
                debugLog("Collision beam axis end at " + end.toShortString() + " must be a beam port in input mode");
                return false;
            }
        }
        return true;
    }

    /**
     * Collision products leave through output beam ports on the two side (width) walls:
     * {@link #REQUIRED_OUTPUTS_PER_SIDE} per wall, {@link #REQUIRED_OUTPUTS} in total.
     * Each output port must reach a chamber camera along a straight run of particle beam blocks.
     */
    private boolean validateOutputPorts() {
        Direction wd = widthDir();
        int left = 0;
        int right = 0;
        for (long packedPos : allBlocks) {
            if (beamPorts.containsKey(packedPos)) {
                continue;
            }
            BlockPos pos = BlockPos.of(packedPos);
            if (!(getBlockEntity(pos) instanceof TargetChamberBeamPortBE port) || !port.isOutput()) {
                continue;
            }
            Direction facing = port.getFacing();
            Direction inward;
            if (facing == wd) {
                right++;
                inward = wd.getOpposite();
            } else if (facing == wd.getOpposite()) {
                left++;
                inward = wd;
            } else {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = pos;
                debugLog("Collision output port at " + pos.toShortString() + " must sit on a side wall");
                return false;
            }
            if (!portLinksToCamera(pos, inward)) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = pos;
                debugLog("Collision output port at " + pos.toShortString() + " is not linked to a chamber camera by particle beam");
                return false;
            }
            beamPorts.put(packedPos, port);
            outputPorts.add(pos);
        }
        if (left != REQUIRED_OUTPUTS_PER_SIDE || right != REQUIRED_OUTPUTS_PER_SIDE) {
            validationResult = ValidationResult.NO_PORT;
            errorBlockPos = controllerBE().getBlockPos();
            debugLog("Collision chamber needs " + REQUIRED_OUTPUTS_PER_SIDE + " output ports per side wall, found left=" + left + " right=" + right);
            return false;
        }
        return true;
    }

    /** Walks inward from a side-wall port through particle beam blocks; true if it reaches a chamber camera. */
    private boolean portLinksToCamera(BlockPos portPos, Direction inward) {
        Block beam = ACCELERATOR_BLOCKS.get("particle_beam").get();
        Block camera = PARTICLE_CHAMBER_BLOCKS.get("target_chamber_camera").get();
        BlockPos pos = portPos.relative(inward);
        for (int i = 0; i < width; i++) {
            BlockState bs = getBlockState(pos);
            if (bs.is(camera)) {
                return true;
            }
            if (!bs.is(beam)) {
                return false;
            }
            pos = pos.relative(inward);
        }
        return false;
    }

    /** Horizontal direction of the width axis (the side walls), matching {@link #getSidePos}. */
    private Direction widthDir() {
        return switch (getMultiblockDirection().ordinal()) {
            case 3 -> Direction.EAST;
            case 5 -> Direction.NORTH;
            case 2 -> Direction.WEST;
            case 4 -> Direction.SOUTH;
            default -> Direction.EAST;
        };
    }

    @Override
    protected CollisionChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (CollisionChamberControllerBE) controllerBe;
    }

    @Override
    public void clearStats() {
        super.clearStats();
        inputPorts.clear();
        outputPorts.clear();
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().setChanged();
    }
}
