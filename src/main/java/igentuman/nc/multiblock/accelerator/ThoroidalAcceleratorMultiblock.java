package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.ElectromagnetBlock;
import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
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
    public boolean isValidForOuter(BlockPos pos) {
        if (getLevel() == null) return false;
        Block block = getBlockState(pos).getBlock();
        if (block == ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get()) return false;
        return validOuterBlocks().contains(block);
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
        controllerPos = BlockPosInstance.copy(controller().controllerBE().getBlockPos());
        multiblockDirection = null;
        controllers.clear();
        connectedPorts = 0;
        width = 0;
        depth = 0;
        height = 0;
        topCasing = 0;
        bottomCasing = 0;
        leftCasing = 0;
        rightCasing = 0;
        outerValid = false;

        if (!resolveRingHeight()) return;
        if (!resolveRingBounds()) return;
        if (!validateCubeShell()) return;

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

    /** Resolves Y bounds from the controller column. Ring must be exactly maxHeight() tall. */
    private boolean resolveRingHeight() {
        BlockPos ctrlPos = controllerPos;
        int top = 0;
        for (int i = 1; i <= maxHeight(); i++) {
            if (!isValidForOuter(new BlockPos(ctrlPos.getX(), ctrlPos.getY() + i, ctrlPos.getZ()))) {
                top = i - 1;
                break;
            }
            top = i;
        }
        int bottom = 0;
        for (int i = 1; i <= maxHeight(); i++) {
            if (!isValidForOuter(new BlockPos(ctrlPos.getX(), ctrlPos.getY() - i, ctrlPos.getZ()))) {
                bottom = i - 1;
                break;
            }
            bottom = i;
        }
        topCasing = top;
        bottomCasing = bottom;
        height = top + bottom + 1;
        if (height != maxHeight()) {
            debugLog("Ring height mismatch: " + height + " != " + maxHeight());
            validationResult = ValidationResult.INCOMPLETE;
            return false;
        }
        return true;
    }

    /**
     * Resolves the cube bounding box (X/Z) by walking 4 horizontal directions at the top Y.
     * Works regardless of whether the controller is on an outer wall, inner wall, or corner.
     */
    private boolean resolveRingBounds() {
        BlockPos ctrlPos = controllerPos;
        int yTop = ctrlPos.getY() + topCasing;
        BlockPos topStart = new BlockPos(ctrlPos.getX(), yTop, ctrlPos.getZ());
        if (!isValidForOuter(topStart)) {
            debugLog("Top of controller column is not casing at " + topStart.toShortString());
            validationResult = ValidationResult.INCOMPLETE;
            return false;
        }

        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        int[] walks = new int[4];
        boolean[] reachesOutside = new boolean[4];
        int maxWalk = maxDepth() + 2;
        int beyondScan = 64;

        for (int d = 0; d < 4; d++) {
            BlockPos cur = topStart;
            int steps = 0;
            while (steps < maxWalk) {
                BlockPos next = cur.relative(dirs[d]);
                if (!isValidForOuter(next)) break;
                cur = next;
                steps++;
            }
            walks[d] = steps;

            BlockPos beyond = cur.relative(dirs[d]);
            boolean foundFurther = false;
            BlockPos scan = beyond;
            for (int i = 0; i < beyondScan; i++) {
                if (isValidForOuter(scan)) {
                    foundFurther = true;
                    break;
                }
                scan = scan.relative(dirs[d]);
            }
            reachesOutside[d] = !foundFurther;
        }

        int axisXSpan = walks[2] + walks[3] + 1;
        int axisZSpan = walks[0] + walks[1] + 1;
        int side = Math.max(axisXSpan, axisZSpan);

        if (side < minDepth()) {
            validationResult = ValidationResult.TOO_SMALL;
            return false;
        }
        if (side > maxDepth()) {
            validationResult = ValidationResult.TOO_BIG;
            return false;
        }

        int cubeMinX, cubeMaxX;
        if (axisXSpan == side) {
            cubeMinX = topStart.getX() - walks[3];
            cubeMaxX = topStart.getX() + walks[2];
        } else {
            boolean eastOut = reachesOutside[2];
            boolean westOut = reachesOutside[3];
            if (eastOut && !westOut) {
                cubeMaxX = topStart.getX() + walks[2];
                cubeMinX = cubeMaxX - side + 1;
            } else if (westOut && !eastOut) {
                cubeMinX = topStart.getX() - walks[3];
                cubeMaxX = cubeMinX + side - 1;
            } else {
                debugLog("Cannot resolve X bounds; E_out=" + eastOut + " W_out=" + westOut);
                validationResult = ValidationResult.WRONG_OUTER;
                return false;
            }
        }

        int cubeMinZ, cubeMaxZ;
        if (axisZSpan == side) {
            cubeMinZ = topStart.getZ() - walks[0];
            cubeMaxZ = topStart.getZ() + walks[1];
        } else {
            boolean northOut = reachesOutside[0];
            boolean southOut = reachesOutside[1];
            if (southOut && !northOut) {
                cubeMaxZ = topStart.getZ() + walks[1];
                cubeMinZ = cubeMaxZ - side + 1;
            } else if (northOut && !southOut) {
                cubeMinZ = topStart.getZ() - walks[0];
                cubeMaxZ = cubeMinZ + side - 1;
            } else {
                debugLog("Cannot resolve Z bounds; N_out=" + northOut + " S_out=" + southOut);
                validationResult = ValidationResult.WRONG_OUTER;
                return false;
            }
        }

        if (cubeMaxX - cubeMinX != cubeMaxZ - cubeMinZ) {
            debugLog("Ring not square: " + (cubeMaxX - cubeMinX + 1) + "x" + (cubeMaxZ - cubeMinZ + 1));
            validationResult = ValidationResult.WRONG_OUTER;
            return false;
        }

        width = side;
        depth = side;
        Direction facing = getControllerDirection();
        switch (facing) {
            case NORTH -> {
                leftCasing = cubeMaxX - ctrlPos.getX();
                rightCasing = ctrlPos.getX() - cubeMinX;
            }
            case SOUTH -> {
                leftCasing = ctrlPos.getX() - cubeMinX;
                rightCasing = cubeMaxX - ctrlPos.getX();
            }
            case EAST -> {
                leftCasing = cubeMaxZ - ctrlPos.getZ();
                rightCasing = ctrlPos.getZ() - cubeMinZ;
            }
            case WEST -> {
                leftCasing = ctrlPos.getZ() - cubeMinZ;
                rightCasing = cubeMaxZ - ctrlPos.getZ();
            }
            default -> {
                leftCasing = ctrlPos.getX() - cubeMinX;
                rightCasing = cubeMaxX - ctrlPos.getX();
            }
        }
        bottomLeft = new BlockPosInstance(cubeMinX, ctrlPos.getY() - bottomCasing, cubeMinZ);
        topRight = new BlockPosInstance(cubeMaxX, yTop, cubeMaxZ);
        multiblockDirection = facing;
        return true;
    }

    /** Validates the outer cube shell (4 outer vertical faces, top, bottom) minus the donut hole. */
    private boolean validateCubeShell() {
        int minX = bottomLeft.getX();
        int minZ = bottomLeft.getZ();
        int maxX = topRight.getX();
        int maxZ = topRight.getZ();
        int minY = bottomLeft.getY();
        int maxY = topRight.getY();

        AABB excludeArea = new AABB(
                new BlockPos(minX + 4, minY - 1, minZ + 4),
                new BlockPos(maxX - 4, maxY + 1, maxZ - 4));
        cacheBlockStates(excludeArea);

        for (int y = minY; y <= maxY; y++) {
            boolean yEdge = (y == minY || y == maxY);
            for (int x = minX; x <= maxX; x++) {
                boolean xEdge = (x == minX || x == maxX);
                for (int z = minZ; z <= maxZ; z++) {
                    boolean zEdge = (z == minZ || z == maxZ);
                    if (!(yEdge || xEdge || zEdge)) continue;
                    if (excludeArea.contains(x + 0.5, y + 0.5, z + 0.5)) continue;

                    BlockPos toCheck = new BlockPos(x, y, z);
                    boolean isCornerEdge = (yEdge && (xEdge || zEdge)) || (xEdge && zEdge);
                    if (isCornerEdge) {
                        if (!isValidCorner(toCheck)) {
                            validationResult = ValidationResult.WRONG_CORNER;
                            errorBlockPos = new BlockPos(toCheck);
                            return false;
                        }
                    } else if (!isValidForOuter(toCheck)) {
                        validationResult = ValidationResult.WRONG_OUTER;
                        errorBlockPos = new BlockPos(toCheck);
                        return false;
                    }
                    processOuterBlock(toCheck);
                }
            }
        }
        return true;
    }


    private boolean validateCornerDipoles() {
        int midY = (bottomLeft.getY() + topRight.getY()) / 2;
        int beamOffset = 2;
        BlockPos sw = new BlockPos(bottomLeft.getX() + beamOffset, midY, bottomLeft.getZ() + beamOffset);
        BlockPos se = new BlockPos(topRight.getX() - beamOffset, midY, bottomLeft.getZ() + beamOffset);
        BlockPos nw = new BlockPos(bottomLeft.getX() + beamOffset, midY, topRight.getZ() - beamOffset);
        BlockPos ne = new BlockPos(topRight.getX() - beamOffset, midY, topRight.getZ() - beamOffset);

        for (BlockPos corner : new BlockPos[]{sw, se, nw, ne}) {
            if (!validateDipoleAt(corner)) return false;
        }
        return true;
    }

    private boolean validateDipoleAt(BlockPos center) {
        Block beamBlock = ACCELERATOR_BLOCKS.get("particle_beam").get();
        Block yokeBlock = ACCELERATOR_BLOCKS.get("electromagnet_yoke").get();

        if (!getBlockState(center).is(beamBlock)) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = new BlockPos(center);
            return false;
        }

        BlockState upState = getBlockState(center.above());
        BlockState downState = getBlockState(center.below());
        if (!(upState.getBlock() instanceof ElectromagnetBlock)) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = new BlockPos(center.above());
            return false;
        }
        if (!(downState.getBlock() instanceof ElectromagnetBlock)) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = new BlockPos(center.below());
            return false;
        }
        if (upState.getBlock() != downState.getBlock()) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = new BlockPos(center.below());
            return false;
        }

        BlockPos[] yokePositions = {
                center.above().north(), center.above().south(),
                center.above().east(),  center.above().west(),
                center.above().north().east(), center.above().north().west(),
                center.above().south().east(), center.above().south().west(),
                center.below().north(), center.below().south(),
                center.below().east(),  center.below().west(),
                center.below().north().east(), center.below().north().west(),
                center.below().south().east(), center.below().south().west(),
                center.north().east(), center.north().west(),
                center.south().east(), center.south().west()
        };
        for (BlockPos pos : yokePositions) {
            if (!getBlockState(pos).is(yokeBlock)) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = new BlockPos(pos);
                return false;
            }
            addIfNotExists(pos, allBlocks);
        }

        BlockPos[] faces = {center.north(), center.south(), center.east(), center.west()};
        for (BlockPos pos : faces) {
            BlockState bs = getBlockState(pos);
            if (!bs.is(beamBlock) && !bs.is(yokeBlock)) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = new BlockPos(pos);
                return false;
            }
            addIfNotExists(pos, allBlocks);
        }

        electromagnets.put(center.above().asLong(), (ElectromagnetBlock) upState.getBlock());
        electromagnets.put(center.below().asLong(), (ElectromagnetBlock) downState.getBlock());
        addIfNotExists(center.above(), allBlocks);
        addIfNotExists(center.below(), allBlocks);
        addIfNotExists(center, allBlocks);
        return true;
    }

    protected void processOuterBlock(BlockPos pos) {
        super.processOuterBlock(pos);
        BlockState bs = getBlockState(pos);
        if(bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())) {
            beamPorts.add(pos.asLong());
        }
    }

    private void indexCornerCoolers() {
        int innerWallOffset = 4;
        BlockPos innerStart = (new BlockPos(bottomLeft)).offset(innerWallOffset, 1, innerWallOffset);
        BlockPos innerEnd = (new BlockPos(topRight)).offset(-innerWallOffset, -4, -innerWallOffset);

        BlockPos innerCorner2 = new BlockPos(innerStart.getX(), innerStart.getY(), innerEnd.getZ());
        BlockPos innerCorner3 = new BlockPos(innerEnd.getX(), innerStart.getY(), innerStart.getZ());

        for (int h = 0; h < 4; h++) {
            processInnerBlock(innerStart.offset(0, h, 0));
            processInnerBlock(innerEnd.offset(0, h, 0));
            processInnerBlock(innerCorner2.offset(0, h, 0));
            processInnerBlock(innerCorner3.offset(0, h, 0));
        }
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
        
        if (!validateInnerWallFace(innerStart.offset(0, 0,1), innerEnd.offset(0, 0,-1), Direction.Axis.X, true)) {
            debugLog("West inner wall validation failed");
            return false;
        }
        
        if (!validateInnerWallFace(innerStart.offset(0, 0,1), innerEnd.offset(0, 0,-1), Direction.Axis.X, false)) {
            debugLog("East inner wall validation failed");
            return false;
        }
        
        if (!validateInnerWallFace(innerStart.offset(1, 0,0), innerEnd.offset(-1, 0,0), Direction.Axis.Z, true)) {
            debugLog("North inner wall validation failed");
            return false;
        }
        
        if (!validateInnerWallFace(innerStart.offset(1, 0,0), innerEnd.offset(-1, 0,0), Direction.Axis.Z, false)) {
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

        if (!validateCornerDipoles()) {
            return;
        }
        indexCornerCoolers();
        initialPos = new BlockPosInstance(bottomLeft.getX()+2, bottomLeft.getY()+2, bottomLeft.getZ()+2);

        multiblockDirection = Direction.SOUTH;
        for (int z = 0; z < Math.max(depth, width)-4; z++) {
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
        for (int z = 0; z < Math.max(depth, width)-6; z++) {
            if(!indexSlice(z)) {
                return;
            }
        }
        multiblockDirection = Direction.WEST;
        for (int z = 1; z < Math.max(depth, width)-6; z++) {
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
            controllerBE().beamLength = beamLength+4;
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

    private static final int RING_WALL_THICKNESS = 4;

    @Override
    public long getCapacityMultiplier() {
        long lx = width();
        long lz = depth();
        long interiorY = Math.max(1L, height() - 2L);
        long outer = (lx - 1L) * (lz - 1L);
        long holeX = lx - 2L * (RING_WALL_THICKNESS + 1);
        long holeZ = lz - 2L * (RING_WALL_THICKNESS + 1);
        long hole = (holeX > 0 && holeZ > 0) ? holeX * holeZ : 0L;
        return (outer - hole) * interiorY;
    }
}
