package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.fusion.FusionCasingBE;
import igentuman.nc.block.entity.fusion.FusionConnectorBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static net.minecraft.core.Direction.*;
import static net.minecraft.world.level.block.Blocks.AIR;

public class FusionReactorMultiblock extends AbstractNCMultiblock {
    protected FusionCoreBE controllerBE;
    protected int length = 0;
    protected boolean connectorsValid = false;
    protected boolean ringValid = false;

    public FusionReactorMultiblock(FusionCoreBE core) {
        super(
                List.of(
                        FUSION_BLOCKS.get("fusion_reactor_casing").get(),
                        FUSION_BLOCKS.get("fusion_reactor_casing_glass").get()
                ),
                List.of(AIR));
        controllerBE = core;
        controller = new FusionReactorController(controllerBE);

    }

    @Override
    public int height() {
        return 3;
    }

    @Override
    public int width() {
        return length;
    }

    @Override
    public int depth() {
        return length;
    }

    @Override
    public int maxHeight() {
        return 3;
    }

    @Override
    public int minHeight() {
        return 3;
    }

    @Override
    public int maxWidth() {
        return 64;
    }

    @Override
    public int minWidth() {
        return 64;
    }

    @Override
    public int maxDepth() {
        return 64;
    }

    @Override
    public int minDepth() {
        return 64;
    }

    @Override
    public final List<Block> validOuterBlocks() {
        return validOuterBlocks;
    }

    @Override
    public List<Block> validInnerBlocks() {
        return validInnerBlocks;
    }

    @Override
    public void validateOuter() {
        validateConnectors();
        validateRing();
        outerValid = ringValid && connectorsValid;
        if(outerValid) {
            validationResult =  ValidationResult.VALID;
        }
    }

    private void validateRing() {
        NCBlockPos pos = new NCBlockPos(controllerBE.getBlockPos().relative(UP));
        ringValid = true;
        for(Direction side: List.of(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+1;
            NCBlockPos startPosInnerWall = null;
            NCBlockPos startPosOuterWall = null;
            NCBlockPos startPosBottomWall = null;
            NCBlockPos startPosTopWall = null;
            Level level = controllerBE.getLevel();
            //position to left corner of the ring
            switch (side) {
                case NORTH -> {
                    dir = EAST;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(NORTH, 2+shift).relative(WEST, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(UP));
                }
                case SOUTH -> {
                    dir = WEST;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(SOUTH, 2+shift).relative(EAST, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(UP));
                }
                case WEST -> {
                    dir = SOUTH;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(WEST, 2+shift).relative(NORTH, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(UP));
                }
                case EAST -> {
                    dir = NORTH;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(EAST, 2+shift).relative(SOUTH, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(UP));
                }
            }
            //inner wall
            for(int i = 0; i < steps; i++) {
                if(level.getBlockEntity(startPosInnerWall.revert().relative(dir, i)) instanceof FusionCasingBE casing) {
                    casing.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosInnerWall));
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosInnerWall);
                    return;
                }
            }
            //outer, bottom, top walls
            for(int i = 0; i < steps+2; i++) {
                if(level.getBlockEntity(startPosOuterWall.revert().relative(dir, i)) instanceof FusionCasingBE casing1) {
                    casing1.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosOuterWall));
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosOuterWall);
                    return;
                }
                if(level.getBlockEntity(startPosBottomWall.revert().relative(dir, i)) instanceof FusionCasingBE casing2) {
                    casing2.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosBottomWall));
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosBottomWall);
                    return;
                }
                if(level.getBlockEntity(startPosTopWall.revert().relative(dir, i)) instanceof FusionCasingBE casing3) {
                    casing3.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosTopWall));

                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosTopWall);
                    return;
                }
            }
        }
    }

    private void validateConnectors() {
        NCBlockPos pos = new NCBlockPos(controllerBE.getBlockPos().above());
        length = 1;
        connectorsValid = true;
        for(int i = 2; i <= maxWidth()/2+1; i++) {
            int connectors = 0;
            for(Direction side: List.of(NORTH, EAST, Direction.SOUTH, Direction.WEST)) {
                if(controllerBE.getLevel().getBlockEntity(pos.revert().relative(side, i)) instanceof FusionConnectorBE connector) {
                    connector.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(pos));
                    connectors++;
                }
            }
            if(connectors == 4) {
                length++;
            } else {
                if (connectors != 0) {
                    connectorsValid = false;
                } else {
                    connectorsValid = true;
                    validationResult = ValidationResult.INCOMPLETE;
                    controller().addErroredBlock(pos);
                }
                return;
            }
        }
    }

    @Override
    public void validateInner() {
        if(!outerValid) return;
        NCBlockPos pos = new NCBlockPos(controllerBE.getBlockPos().relative(UP));
        innerValid = true;
        for(Direction side: List.of(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+2;
            NCBlockPos innerRingStartPos = null;
            Level level = controllerBE.getLevel();
            //position to left corner of the ring
            switch (side) {
                case NORTH -> {
                    dir = EAST;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                }
                case SOUTH -> {
                    dir = WEST;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                }
                case WEST -> {
                    dir = SOUTH;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                }
                case EAST -> {
                    dir = NORTH;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                }
            }
            for(int i = 0; i < steps; i++) {
                if(!validateInnerBlock(innerRingStartPos.revert().relative(dir, i))) {
                    innerValid = false;
                    validationResult = ValidationResult.WRONG_INNER;
                    controller().addErroredBlock(innerRingStartPos);
                    return;
                }
            }
        }
        validationResult =  ValidationResult.VALID;
    }

    @Override
    protected boolean validateInnerBlock(BlockPos toCheck) {
        return validInnerBlocks.contains(level().getBlockState(toCheck).getBlock());
    }

    private Level level() {
        return controllerBE.getLevel();
    }

    @Override
    protected void invalidateStats() {
        length = 0;
        controller().clearStats();
        isFormed = false;
    }

    @Override
    protected Direction getFacing() {
        return null;
    }
}
