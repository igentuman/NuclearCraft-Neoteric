package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class ThoroidalAcceleratorMultiblock extends AbstractAcceleratorMultiblock {

    private RingAcceleratorControllerBE controllerBe;

    public ThoroidalAcceleratorMultiblock(RingAcceleratorControllerBE controller) {
        super(
                getBlocksByTagKey(ACCELERATOR_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(ACCELERATOR_INNER_BLOCKS.location().toString()),
                new ThoroidalAcceleratorController(controller)
        );
        id = "ring_accelerator_"+controller.getBlockPos().toShortString();
        controllerBe = controller;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public ThoroidalAcceleratorController controller() {
        return (ThoroidalAcceleratorController) controller;
    }

    @Override
    protected RingAcceleratorControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return controllerBe;
    }

    @Override
    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    @Override
    public void clearStats() {
        controller().clearStats();
    }

    @Override
    public void validateOuter() {
        topRight = null;
        bottomLeft = null;
        initialPos = null;
        beamPorts.clear();
        validationResult = ValidationResult.INCOMPLETE;
        stage = FINAL_STAGE;
        initialPos = BlockPosInstance.copy(controller().controllerBE().getBlockPos());
        multiblockDirection = null;
        controllers.clear();
        connectedPorts = 0;
        width = 0;
        depth = 0;
        height = 0;
        outerValid = false;
        resolveHeight();
        if(height != maxHeight()) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }
        resolveDepth();
        resolveWidth();


        final boolean controllerOnSide = isControllerPlacedOnSide();

        if(controllerOnSide) {
            if(width > maxWidth()) {
                validationResult = ValidationResult.TOO_BIG;
                return;
            }
            if(width < minWidth()) {
                validationResult = ValidationResult.TOO_SMALL;
                return;
            }
            resolveRealDepth();
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
        }
        BlockPos leftFront = new BlockPosInstance(getLeftPos(leftCasing));
        BlockPos leftBack = new BlockPosInstance(getLeftPos(leftCasing).relative(getControllerDirection(), -depth+1));
        BlockPos rightFront = new BlockPosInstance(getRightPos(rightCasing));
        BlockPos rightBack = new BlockPosInstance(getRightPos(rightCasing).relative(getControllerDirection(), -depth+1));
        int minX = MathUtils.min(leftFront.getX(), rightFront.getX(), leftBack.getX(), rightBack.getX());
        int minZ = MathUtils.min(leftFront.getZ(), rightFront.getZ(), leftBack.getZ(), rightBack.getZ());
        int maxX = MathUtils.max(leftFront.getX(), rightFront.getX(), leftBack.getX(), rightBack.getX());
        int maxZ = MathUtils.max(leftFront.getZ(), rightFront.getZ(), leftBack.getZ(), rightBack.getZ());
        bottomLeft = new BlockPosInstance(minX, leftFront.getY() - bottomCasing, minZ);
        topRight = new BlockPosInstance(maxX, leftFront.getY() + topCasing, maxZ);
        AABB excludeArea = AABB.encapsulatingFullBlocks(new BlockPos(bottomLeft).offset(4, -1, 4), new BlockPos(topRight).offset(-4, 1, -4));
        cacheBlockStates(excludeArea);
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    BlockPos toCheck = getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                    if(excludeArea.contains(toCheck.getCenter())) {
                        continue;
                    }
                    if (y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        //validate corner blocks
                        if (((y == 0 || y == height-1) && (z == 0 || z == depth - 1))
                                || ((y == 0 || y == height-1) && (x == 0 || x == width - 1))
                                || ((z == 0 || z == depth-1) && (x == 0 || x == width - 1))
                        ) {
                            if (!isValidCorner(toCheck)) {
                                validationResult = ValidationResult.WRONG_CORNER;
                                errorBlockPos = new BlockPos(toCheck);
                                return;
                            }
                        } else if (!isValidForOuter(toCheck)) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            errorBlockPos = new BlockPos(toCheck);
                            return;
                        }
                        processOuterBlock(toCheck);
                    }
                }
            }
        }

        BlockPos startingPos = new BlockPos(bottomLeft);
        BlockPos endingPos = new BlockPos(topRight);

        if (!validateInnerWalls(startingPos, endingPos)) {
            return;
        }


        if (controllers.size() > 1) {
            validationResult = ValidationResult.TOO_MANY_CONTROLLERS;
            return;
        }

        validationResult = ValidationResult.VALID;
        outerValid = true;
        stage = 1;
        hasToRefresh = true;
    }

    protected void processOuterBlock(BlockPos pos) {
        super.processOuterBlock(pos);
        BlockState bs = getBlockState(pos);
        if(bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())) {
            beamPorts.add(pos.asLong());
        }
    }

    private void resolveRealDepth() {
        debugLog("Resolving depth from position " + initialPos().toShortString());

        for(int i = 1; i <= maxDepth()+2; i++) {
            if (!isValidForOuter(getLeftPos(leftCasing).below(bottomCasing).relative(getControllerDirection(), -i))) {
                depth = i;
                debugLog("Found depth boundary at offset " + i);
                break;
            }
        }

        debugLog("Resolved real depth: " + depth);
    }

    /**
     * Validates the 4 inner walls of the toroidal ring structure.
     * The accelerator has a 5x5 cross-section slice forming a ring.
     * The inner walls are the faces that border the hollow center of the torus.
     */
    private boolean validateInnerWalls(BlockPos startingPos, BlockPos endingPos) {
        debugLog("Validating inner walls of toroidal accelerator ring");
        
        int innerWallOffset = 4;
        
        BlockPos innerStart = startingPos.offset(innerWallOffset, 0, innerWallOffset);
        BlockPos innerEnd = endingPos.offset(-innerWallOffset, 0, -innerWallOffset);
        
        debugLog("Inner wall boundaries: " + innerStart.toShortString() + " to " + innerEnd.toShortString());
        
        if (!validateInnerWallFace(innerStart, innerEnd, Direction.Axis.X, true)) {
            debugLog("West inner wall validation failed");
            return false;
        }
        
        if (!validateInnerWallFace(innerStart, innerEnd, Direction.Axis.X, false)) {
            debugLog("East inner wall validation failed");
            return false;
        }
        
        if (!validateInnerWallFace(innerStart, innerEnd, Direction.Axis.Z, true)) {
            debugLog("North inner wall validation failed");
            return false;
        }
        
        if (!validateInnerWallFace(innerStart, innerEnd, Direction.Axis.Z, false)) {
            debugLog("South inner wall validation failed");
            return false;
        }
        
        debugLog("All inner walls validated successfully");
        return true;
    }

    /**
     * Validates a single inner wall face of the toroidal ring structure.
     * @param innerStart The starting position of the inner boundary
     * @param innerEnd The ending position of the inner boundary  
     * @param axis The axis along which this wall face runs
     * @param isMinSide Whether this is the minimum side (true) or maximum side (false) of the axis
     */
    private boolean validateInnerWallFace(BlockPos innerStart, BlockPos innerEnd, Direction.Axis axis, boolean isMinSide) {
        debugLog("Validating inner wall face along " + axis + " axis, " + (isMinSide ? "min" : "max") + " side");
        
        // Determine the wall position based on axis and side
        int wallCoordinate;
        switch (axis) {
            case X -> wallCoordinate = isMinSide ? innerStart.getX() : innerEnd.getX();
            case Z -> wallCoordinate = isMinSide ? innerStart.getZ() : innerEnd.getZ();
            default -> {
                debugLog("Invalid axis for inner wall: " + axis);
                return false;
            }
        }
        
        // Validate each block in the wall face
        for (int y = innerStart.getY(); y <= innerEnd.getY(); y++) {
            if (axis == Direction.Axis.X) {
                // Wall perpendicular to X-axis (runs along Z-axis)
                for (int z = innerStart.getZ(); z <= innerEnd.getZ(); z++) {
                    BlockPos toCheck = new BlockPos(wallCoordinate, y, z);
                    if (!validateInnerWallBlock(toCheck, y, innerStart.getY(), innerEnd.getY())) {
                        return false;
                    }
                }
            } else if (axis == Direction.Axis.Z) {
                // Wall perpendicular to Z-axis (runs along X-axis)
                for (int x = innerStart.getX(); x <= innerEnd.getX(); x++) {
                    BlockPos toCheck = new BlockPos(x, y, wallCoordinate);
                    if (!validateInnerWallBlock(toCheck, y, innerStart.getY(), innerEnd.getY())) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * Validates a single block in an inner wall face.
     * @param toCheck The block position to validate
     * @param y The Y coordinate of the block
     * @param minY The minimum Y coordinate of the wall
     * @param maxY The maximum Y coordinate of the wall
     */
    private boolean validateInnerWallBlock(BlockPos toCheck, int y, int minY, int maxY) {
        // Determine if this is a corner block
        boolean isCorner = y == minY || y == maxY;
        
        if (isCorner) {
            if (!isValidCorner(toCheck)) {
                validationResult = ValidationResult.WRONG_CORNER;
                errorBlockPos = new BlockPos(toCheck);
                debugLog("Invalid inner corner block at " + toCheck.toShortString());
                return false;
            }
        } else {
            if (!isValidForOuter(toCheck)) {
                validationResult = ValidationResult.WRONG_OUTER;
                errorBlockPos = new BlockPos(toCheck);
                debugLog("Invalid inner wall block at " + toCheck.toShortString());
                return false;
            }
        }
        
        // Process the block for the multiblock
        processOuterBlock(toCheck);
        return true;
    }

    protected void validateBeam() {
        stage = FINAL_STAGE;
        depth-=2;
        width-=2;
        centerPos = new BlockPosInstance(bottomLeft.getX()+2, bottomLeft.getY()+2, bottomLeft.getZ()+1);
        multiblockDirection = Direction.SOUTH;
        int tmpLength = 0;
        super.validateBeam();
        if(!validationResult.isValid) return;
        tmpLength += beamLength;
        centerPos = new BlockPosInstance(bottomLeft.getX()+1, bottomLeft.getY()+2, bottomLeft.getZ()+2);
        multiblockDirection = Direction.EAST;
        super.validateBeam();
        if(!validationResult.isValid) return;
        tmpLength += beamLength;
        centerPos = new BlockPosInstance(topRight.getX()-2, topRight.getY()-2, topRight.getZ()-1);
        multiblockDirection = Direction.NORTH;
        super.validateBeam();
        if(!validationResult.isValid) return;
        tmpLength += beamLength;
        centerPos = new BlockPosInstance(topRight.getX()-1, topRight.getY()-2, topRight.getZ()-2);
        multiblockDirection = Direction.WEST;
        super.validateBeam();
        if(!validationResult.isValid) return;
        tmpLength += beamLength;
        beamLength = tmpLength-12;
        depth+=2;
        width+=2;
        for(long packed: beamPorts) {
            Direction dir = getBlockState(packed).getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos pos = BlockPos.of(packed);
            for(int i=1; i<7; i++) {
                if(!getBlockState(pos.relative(dir.getOpposite(), i)).is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = new BlockPosInstance(pos.relative(dir.getOpposite(), i));
                    innerValid = false;
                    stage = FINAL_STAGE;
                    return;
                }
            }
        }
        stage = 2;
    }

    @Override
    public void indexInnerBlocks() {
        stage = FINAL_STAGE;
        innerValid = false;
        acceleratingVoltage = 0;
        heatRate = 0;
        maxTemperature = Integer.MAX_VALUE;
        efficiency = 0.0;
        quadrupolesCount = 0;
        dipolesCount = 0;
        quadStrength = 0;
        dipoleStrength = 0;
        focus = 0.0;
        energyRequired = 0;
        initialPos = new BlockPosInstance(bottomLeft.getX()+2, bottomLeft.getY()+2, bottomLeft.getZ()+2);

        multiblockDirection = Direction.SOUTH;
        for (int z = 1; z < Math.max(depth, width)-4; z++) {
            if(!indexSlice(z)) {
                return;
            }
        }
        multiblockDirection = Direction.EAST;
        for (int z = 1; z < Math.max(depth, width)-4; z++) {
            if(!indexSlice(z)) {
                return;
            }
        }
        initialPos = new BlockPosInstance(topRight.getX()-2, topRight.getY()-2, topRight.getZ()-2);
        multiblockDirection = Direction.NORTH;
        for (int z = 1; z < Math.max(depth, width)-4; z++) {
            if(!indexSlice(z)) {
                return;
            }
        }
        multiblockDirection = Direction.WEST;
        for (int z = 1; z < Math.max(depth, width)-4; z++) {
            if(!indexSlice(z)) {
                return;
            }
        }

        innerValid = true;
        validationResult =  ValidationResult.VALID;
        stage = 3;
    }

    @Override
    public void validate() {
        long startTime = System.nanoTime();
        switch (stage) {
            case 0 -> validateOuter();
            case 1 -> validateBeam();
            case 2 -> indexInnerBlocks();
            case 3 -> veryfyCoolers();
        }
        debugLog("Accelerator validate stage " + stage + " " + initialPos().toShortString() + " in " + (System.nanoTime() - startTime)/1000000 + "ms " + validationResult);
        if(stage < FINAL_STAGE) {
            hasToRefresh = true;
            return;
        }

        isFormed = outerValid && innerValid;
        focus = quadStrength + dipoleStrength/2D;
        if (isFormed) {
            validationResult = ValidationResult.VALID;
            errorBlockPos = BlockPos.ZERO;
            controllerBE().ionSourcePos = ionSourcePos;
            controllerBE().beamLength = beamLength;
            controllerBE().amplifiers = amplifiers.size();
            controllerBE().coolers = validCoolers;
            controllerBE().quadroupoles = quadrupolesCount;
            controllerBE().dipoles = dipolesCount;
            controllerBE().focus = focus;
            controllerBE().maxTemperature = maxTemperature;
            controllerBE().heatRate = heatRate;
            controllerBE().efficiency = efficiency/(amplifiers.size() + electromagnets.size());
            controllerBE().quadStrength = quadStrength;
            controllerBE().dipoleStrength = dipoleStrength;
            controllerBE().acceleratingVoltage = acceleratingVoltage;
            controllerBE().energyRequired = energyRequired;
            controllerBE().coolingRate = coolingRate;
            hasToRefresh = false;
        } else {
            clearStats();
        }

        controllerBE().setChanged();

        stage = 0;
    }
}
