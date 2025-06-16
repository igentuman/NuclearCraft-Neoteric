package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class LinearAcceleratorMultiblock extends AbstractAcceleratorMultiblock {

    private LinearAcceleratorControllerBE controllerBe;

    public LinearAcceleratorMultiblock(LinearAcceleratorControllerBE controller) {
        super(
                getBlocksByTagKey(ACCELERATOR_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(ACCELERATOR_INNER_BLOCKS.location().toString()),
                new LinearAcceleratorController(controller)
        );
        id = "linear_accelerator_"+controller.getBlockPos().toShortString();
        controllerBe = controller;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public LinearAcceleratorController controller() {
        return (LinearAcceleratorController) controller;
    }

    @Override
    protected LinearAcceleratorControllerBE controllerBE() {
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
        cacheBlockStates();
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        //validate corner blocks
                        if (((y == 0 || y == height-1) && (z == 0 || z == depth - 1))
                                || ((y == 0 || y == height-1) && (x == 0 || x == width - 1))
                                || ((z == 0 || z == depth-1) && (x == 0 || x == width - 1))
                        ) {
                            if (!isValidCorner(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z))) {
                                validationResult = ValidationResult.WRONG_CORNER;
                                errorBlockPos = new BlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                                return;
                            }
                        } else if (!isValidForOuter(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z))) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            errorBlockPos = new BlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                            return;
                        }
                        processOuterBlock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    }
                }
            }
        }



        if(maxX - minX == 4) {
            centerPos = new BlockPosInstance((minX + maxX) / 2, bottomLeft.getY() + 2, minZ);
            multiblockDirection = Direction.NORTH;
        }
        if(maxZ - minZ == 4) {
            centerPos = new BlockPosInstance(minX, bottomLeft.getY() + 2, (minZ+maxZ)/2);
            multiblockDirection = Direction.WEST;
        }

        BlockState bs = getBlockState(centerPos);
        if(!bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get()) && !bs.is(ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get())) {
            validationResult = ValidationResult.WRONG_BLOCK;
            errorBlockPos = new BlockPosInstance(centerPos);
            return;
        }
        BlockPos ionSourcePos = bs.is(ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get()) ? new BlockPosInstance(centerPos) : BlockPos.ZERO;
        if(maxX - minX == 4) {
            centerPos = new BlockPosInstance((minX + maxX) / 2, bottomLeft.getY() + 2, maxZ);
            multiblockDirection = Direction.NORTH;
        }
        if(maxZ - minZ == 4) {
            centerPos = new BlockPosInstance(maxX, bottomLeft.getY() + 2, (minZ+maxZ)/2);
            multiblockDirection = Direction.WEST;
        }
        bs = getBlockState(centerPos);
        if(!bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get()) && !bs.is(ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get())) {
            validationResult = ValidationResult.WRONG_BLOCK;
            errorBlockPos = new BlockPosInstance(centerPos);
            return;
        }
        if(!ionSourcePos.equals(BlockPos.ZERO) && bs.is(ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get())) {
            validationResult = ValidationResult.WRONG_BLOCK;
            errorBlockPos = new BlockPosInstance(centerPos);
            return;
        }

        centerPos.revert();
        if (controllers.size() > 1) {
            validationResult = ValidationResult.TOO_MANY_CONTROLLERS;
            return;
        }
        validationResult = ValidationResult.VALID;
        outerValid = true;
        stage = 1;
        hasToRefresh = true;
    }

    @Override
    public void validate() {
        long startTime = System.nanoTime();
        switch (stage) {
            case 0 -> validateOuter();
            case 1 -> validateBeam();
            case 2 -> indexInnerBlocks();
            case 3 -> indexCoolers();
        }
        debugLog("Accelerator validate stage " + stage + " " + initialPos().toShortString() + " in " + (System.nanoTime() - startTime)/1000000 + "ms " + validationResult);
        if(stage < FINAL_STAGE) {
            hasToRefresh = true;
            return;
        }

        isFormed = outerValid && innerValid;

        if (isFormed) {
            validationResult = ValidationResult.VALID;
            errorBlockPos = BlockPos.ZERO;
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
