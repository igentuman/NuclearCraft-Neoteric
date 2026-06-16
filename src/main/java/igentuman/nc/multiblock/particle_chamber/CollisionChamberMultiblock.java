package igentuman.nc.multiblock.particle_chamber;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.target_chamber.DetectorBlock;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public final List<BlockPos> chamberCameras = new ArrayList<>();

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

    public boolean isControllerPlacedOnSide() {
        return depth > 4 && depth < minDepth();
    }

    @Override
    public void validateOuter() {
        debugLog("Particle chamber outer validation for " + getClass().getSimpleName());
        outerValid = false;
        inputPorts.clear();
        outputPorts.clear();
        beamPorts.clear();
        chamberCameras.clear();
        validDetectors.clear();
        allDetectors.clear();
        resolveDimensions();
        if(height > maxHeight()) {
            validationResult = ValidationResult.TOO_BIG;
            return;
        }
        if(height < minHeight()) {
            validationResult = ValidationResult.TOO_SMALL;
            return;
        }
        final boolean controllerOnSide = isControllerPlacedOnSide();

        if(controllerOnSide) {
            if(width > maxDepth()) {
                validationResult = ValidationResult.TOO_BIG;
                return;
            }
            if(width < minDepth()) {
                validationResult = ValidationResult.TOO_SMALL;
                return;
            }
            if(height != depth) {
                validationResult = ValidationResult.WRONG_PROPORTIONS;
                return;
            }
        }
        if(!controllerOnSide) {
            if(depth > maxDepth()) {
                validationResult = ValidationResult.TOO_BIG;
                return;
            }
            if(depth < minDepth()) {
                validationResult = ValidationResult.TOO_SMALL;
                return;
            }
            if(height != width) {
                validationResult = ValidationResult.WRONG_PROPORTIONS;
                return;
            }
        }
        findCorners();

        cacheBlockStates(null);
        debugLog("Cached block states for validation area. Corners: " + bottomLeft.toShortString() + " to " + topRight.toShortString() +
                ", Total cached: " + bsCache.size());

        int totalOuterBlocks = 0;
        int validOuterBlocks = 0;
        int cornerBlocks = 0;
        int validCornerBlocks = 0;
        Direction controllerDirection = getControllerDirection();
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        BlockPos currentPos = getSidePos(x - leftCasing).above(y - bottomCasing).relative(controllerDirection, -z);
                        totalOuterBlocks++;

                        if (((y == 0 || y == height-1) && (z == 0 || z == depth - 1))
                                || ((y == 0 || y == height-1) && (x == 0 || x == width - 1))
                                || ((z == 0 || z == depth-1) && (x == 0 || x == width - 1))
                        ) {
                            cornerBlocks++;
                            if (!isValidCorner(currentPos)) {
                                validationResult = ValidationResult.WRONG_CORNER;
                                errorBlockPos = new BlockPos(currentPos);
                                debugLog("Validation failed - WRONG_CORNER at " + currentPos.toShortString() +
                                        " - Expected corner block, found: " + getBlockState(currentPos).getBlock().getDescriptionId());
                                return;
                            }
                            validCornerBlocks++;
                        } else if (!isValidForOuter(currentPos)) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            errorBlockPos = new BlockPos(currentPos);
                            debugLog("Validation failed - WRONG_OUTER at " + currentPos.toShortString() +
                                    " - Expected outer block, found: " + getBlockState(currentPos).getBlock().getDescriptionId());
                            return;
                        } else {
                            validOuterBlocks++;
                        }
                        processOuterBlock(currentPos);
                    }
                }
            }
        }

        debugLog("Outer block validation complete - Total: " + totalOuterBlocks +
                ", Valid outer: " + validOuterBlocks + ", Corner blocks: " + cornerBlocks +
                ", Valid corners: " + validCornerBlocks);

        if (controllers.size() > 1) {
            validationResult = ValidationResult.TOO_MANY_CONTROLLERS;
            debugLog("Validation failed - TOO_MANY_CONTROLLERS: Found " + controllers.size() + " controllers");
            return;
        }
        outerValid = true;
        validationResult = ValidationResult.VALID;
    }

    @Override
    protected void processOuterBlock(BlockPos pos) {
        super.processOuterBlock(pos);
        if(getBlockEntity(pos) instanceof TargetChamberBeamPortBE be) {
            beamPorts.put(pos.asLong(), be);
        }
    }

    @Override
    public void validateInner() {
        debugLog("Collision chamber inner validation");
        if (!outerValid) {
            clearStats();
            return;
        }


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
        validateDetectors();
        controllerBE().allDetectors = allDetectors.size();
        controllerBE().efficiency = efficiency * 100;
        controllerBE().energyPerTick = power;
        controllerBE().connectedPorts = connectedPorts;
        controllerBE().detectorsCount = validDetectors.size();
        controllerBE().markDirty();
        ctrl.height = height;
        ctrl.width = width;
        ctrl.depth = depth;
        validationResult = ValidationResult.VALID;
        innerValid = true;
    }

    public Map<Long, DetectorBlock> validateDetectors() {
        validDetectors.clear();
        for (long packedPos : allDetectors) {
            BlockPos hpos = BlockPos.of(packedPos);
            Block block = getBlockState(hpos).getBlock();
            if (block instanceof DetectorBlock hs) {
                if (hs.isValid(getLevel(), hpos, closestChamberPos(hpos))) {
                    validDetectors.put(packedPos, hs);
                }
            }
        }
        controllerBE().detectorsCount = validDetectors.size();
        return validDetectors;
    }

    private BlockPos closestChamberPos(BlockPos hpos) {
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos camera : chamberCameras) {
            double d = camera.distSqr(hpos);
            if (d < best) {
                best = d;
                closest = camera;
            }
        }
        return closest;
    }

    protected Direction getMultiblockDirection() {
        if(multiblockDirection == null) {
            multiblockDirection = getControllerDirection();
        }
        return multiblockDirection;
    }

    /**
     * Walks the beam line along the depth (controller-facing) axis through the chamber centre.
     * The {@code depth - 2} interior slots must be particle beam blocks carrying at least
     * {@link #REQUIRED_CAMERAS} chamber cameras; both end caps must be beam ports in input mode.
     */
    private boolean validateBeamAxis() {
        int xc = width / 2;
        if (isControllerPlacedOnSide()) {
            xc = depth / 2;
        }

        int yc = height / 2;

        Direction dir = switch (getControllerDirection().getOpposite()) {
            case WEST -> Direction.EAST;
            case NORTH -> Direction.SOUTH;
            default -> getControllerDirection().getOpposite();
        };

        if(isControllerPlacedOnSide()) {
            dir = switch (dir) {
                case NORTH, SOUTH -> Direction.EAST;
                case EAST, WEST -> Direction.SOUTH;
                default -> dir;
            };
        }
        Block beam = ACCELERATOR_BLOCKS.get("particle_beam").get();
        Block camera = PARTICLE_CHAMBER_BLOCKS.get("target_chamber_camera").get();
        int length = depth;
        if (isControllerPlacedOnSide()) {
            length = width;
        }
        if(dir.equals(Direction.EAST)) {
            xc *= -1;
        }
        BlockPosInstance pos = BlockPosInstance.of(((BlockPosInstance)bottomLeft).revert().above(yc).relative(dir.getCounterClockWise(), xc).asLong());
        ((BlockPosInstance) bottomLeft).revert();
        int cameras = 0;
        for (int z = 1; z < length - 1; z++) {

            BlockState bs = getBlockState(pos.revert().relative(dir, z));
            if (bs.is(camera)) {
                cameras++;
                chamberCameras.add(new BlockPos(pos.revert().relative(dir, z)));
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

        BlockPos near = new BlockPos(pos.revert());
        BlockPos far = new BlockPos(pos.revert().relative(dir, length-1));
        for (BlockPos end : List.of(near, far)) {
            if (getBlockEntity(end) instanceof TargetChamberBeamPortBE port && port.isInput()) {
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
        HashMap<Direction, Integer> portsPerSide = new HashMap<>();
        for (long packedPos : beamPorts.keySet()) {
            BlockPos pos = BlockPos.of(packedPos);
            if (!(getBlockEntity(pos) instanceof TargetChamberBeamPortBE port) || !port.isOutput()) {
                continue;
            }
            Direction facing = port.getFacing();
            if ((isControllerPlacedOnSide() && facing != getMultiblockDirection() && facing != getMultiblockDirection().getOpposite())
            || (!isControllerPlacedOnSide() && (facing == getMultiblockDirection() || facing == getMultiblockDirection().getOpposite()))) {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = pos;
                debugLog("Collision output port at " + pos.toShortString() + " must sit on a side wall");
                return false;
            }
            if (!portLinksToCamera(pos, facing.getOpposite())) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = pos;
                debugLog("Collision output port at " + pos.toShortString() + " is not linked to a chamber camera by particle beam");
                return false;
            }
            portsPerSide.put(facing, portsPerSide.getOrDefault(facing, 0) + 1);
            outputPorts.add(pos);
        }
        if (outputPorts.size() != 4 || portsPerSide.size() != 2) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = new BlockPos(controllerPos);
            debugLog("Expected 4 output beam ports along 2 walls");
            return false;
        }
        for(int qty: portsPerSide.values()) {
            if(qty != 2) {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = controllerBE().getBlockPos();
                debugLog("Expected 2 output beam ports along 2 walls, found " + qty);
                return false;

            }
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
