package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_CASING_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class LinearAcceleratorMultiblock extends AbstractMultiblock {

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
    public int maxHeight() {
        return 5;
    }
    @Override
    public int minHeight() {
        return 5;
    }
    @Override
    public int maxWidth() {
        return 5;
    }
    @Override
    public int minWidth() {
        return 5;
    }
    @Override
    public int maxDepth() {
        return switch (ACCELERATOR_CONFIG.SCALE.get()) {
            case 2 -> 1000;
            case 3 -> 10000;
            default -> 100;
        };
    }
    @Override
    public int minDepth() {
        return switch (ACCELERATOR_CONFIG.SCALE.get()) {
            case 2 -> 60;
            case 3 -> 600;
            default -> 6;
        };
    }

    private LinearAcceleratorControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = (LinearAcceleratorControllerBE) controller().controllerBE();
        }
        return controllerBe;
    }

    @Override
    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    @Override
    public void invalidateStats() {
        controller().clearStats();
    }

    public boolean isControllerPlacedOnSide() {
        for(int i = 1; i<maxWidth(); i++) {
            if (!isValidForOuter(getForwardPos(i).above(topCasing))) {
                width = i;
                break;
            }
            updateDimensions(getForwardPos(i).above(topCasing));
            width = i + 1;
        }
        if(isValidForOuter(getForwardPos(5).above(topCasing))) {
            width = 0;
        }
        return width == maxWidth();
    }

    @Override
    public void validate() {
        // Reset all validation state
        topRight = null;
        bottomLeft = null;
        validationResult = ValidationResult.INCOMPLETE;
        allBlocks.clear();
        controllers.clear();
        bsCache.clear();
        beCache.clear();
        width = 0;
        depth = 0;
        height = 0;
        outerValid = false;
        initialPos = NCBlockPos.copy(controller().controllerBE().getBlockPos());
        multiblockDirection = null;
        
        // Step 1: Detect height
        resolveHeight();
        if(height != maxHeight()) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }
        
        // Step 2: Detect controller placement (on side or on face)
        boolean controllerOnSide = isControllerPlacedOnSide();

        if(!controllerOnSide) {
            switch (getControllerDirection()) {
                case SOUTH -> multiblockDirection = Direction.NORTH;
                case WEST -> multiblockDirection = Direction.EAST;
            }
            resolveWidth();
            resolveDepth();
            BlockPos tmp = new BlockPos(topRight);
            initialPos = NCBlockPos.copy(tmp.below(4));
        } else {
            for(int i = 1; i<maxDepth(); i++) {
                if (!isValidForOuter(getLeftPos(i).above(topCasing))) {
                    depth = i;
                    break;
                }
                updateDimensions(getLeftPos(i));
            }
            for(int i = 1; i<maxDepth(); i++) {
                if (!isValidForOuter(getRightPos(i).above(topCasing))) {
                    depth += i-1;
                    break;
                }
                updateDimensions(getRightPos(i));
            }

            //determine multiblock facing, multiblock facing always towards positive coordinates
            switch (getControllerDirection()) {
                case SOUTH -> multiblockDirection = Direction.EAST;
                case WEST -> multiblockDirection = Direction.SOUTH;
                case EAST -> multiblockDirection = Direction.SOUTH;
                case NORTH -> multiblockDirection = Direction.EAST;
            }
            initialPos = NCBlockPos.copy(topRight);
        }
        outerValid = validationResult.isValid;
        innerValid = validationResult.isValid;
        isFormed = outerValid && innerValid;
        controller().setErroredBlock(initialPos);
        if (isFormed) {
            validationResult = ValidationResult.VALID;
        } else {
            controller.clearStats();
        }
        debugLog("NC multiblock was validated at " + initialPos().toShortString());
    }
    

}
