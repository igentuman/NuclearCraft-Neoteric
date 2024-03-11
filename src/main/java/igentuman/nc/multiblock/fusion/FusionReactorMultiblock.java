package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.ElectromagnetBE;
import igentuman.nc.block.entity.RFAmplifierBE;
import igentuman.nc.block.entity.fusion.FusionCasingBE;
import igentuman.nc.block.entity.fusion.FusionConnectorBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.handler.config.FusionConfig.FUSION_CONFIG;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static net.minecraft.block.Blocks.AIR;
import static net.minecraft.util.Direction.*;

public class FusionReactorMultiblock extends AbstractNCMultiblock {
    public int magnetsEfficiency = 0;
    public int rfEfficiency = 0;
    protected FusionCoreBE<?> controllerBE;
    protected int length = 0;
    public double magneticFieldStrength = 0;
    public int magnetsPower = 0;
    public int maxMagnetsTemp = 0;
    //KEV
    public int rfAmplification = 0;
    public int rfAmplifiersPower = 0;
    public int maxRFAmplifiersTemp = 0;
    protected boolean connectorsValid = false;
    protected boolean ringValid = false;
    protected boolean needToCollectFunctionalBlocks = true;
    public boolean needToRecalculateCharacteristics = true;

    public boolean isReadyToProcess()
    {
        return isFormed && outerValid && innerValid && !needToRecalculateCharacteristics && !needToCollectFunctionalBlocks;
    }

    protected HashMap<BlockPos, ElectromagnetBE> electromagnets = new HashMap<>();
    protected HashMap<BlockPos, RFAmplifierBE> amplifiers = new HashMap<>();

    public FusionReactorMultiblock(FusionCoreBE<?> core) {
        super(
                Arrays.asList(
                        FUSION_BLOCKS.get("fusion_reactor_casing").get(),
                        FUSION_BLOCKS.get("fusion_reactor_casing_glass").get()
                ),
                Arrays.asList(AIR));
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
        return FUSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minWidth() {
        return FUSION_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int maxDepth() {
        return FUSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minDepth() {
        return FUSION_CONFIG.MIN_SIZE.get();
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

    @Override
    public void validate() {
        super.validate();
        needToCollectFunctionalBlocks = isFormed;
    }

    @Override
    public void onBlockDestroyed(BlockState state, World level, BlockPos pos, Explosion explosion) {
        if(controllerBE.plasmaTemperature > 100000) {
            level.explode(null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    1, true, Explosion.Mode.NONE);
        }
        controller.clearStats();
    }

    public void collectFunctionalParts() {
        electromagnets.clear();
        amplifiers.clear();
        NCBlockPos pos = new NCBlockPos(controllerBE.getBlockPos());
        for(Direction side: Arrays.asList(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+1;
            NCBlockPos startPosInnerWall = null;
            NCBlockPos startPosOuterWall = null;
            World level = controllerBE.getLevel();
            //position to left corner of the ring
            switch (side) {
                case NORTH:
                    dir = EAST;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(NORTH, 2+shift).relative(WEST, 1+shift));
                break;
                case SOUTH:
                    dir = WEST;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(SOUTH, 2+shift).relative(EAST, 1+shift));
                    break;

                case WEST:
                    dir = SOUTH;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(WEST, 2+shift).relative(NORTH, 1+shift));
                    break;

                case EAST:
                    dir = NORTH;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(EAST, 2+shift).relative(SOUTH, 1+shift));
                    break;
            }
            //inner
            for(int i = 0; i < steps; i++) {
                TileEntity te = level.getBlockEntity(startPosInnerWall.revert().relative(dir, i));
                if(te instanceof ElectromagnetBE) {
                    electromagnets.put(new NCBlockPos(startPosInnerWall), (ElectromagnetBE) te);
                } else if(te instanceof RFAmplifierBE) {
                    amplifiers.put(new NCBlockPos(startPosInnerWall), (RFAmplifierBE) te);
                }
                TileEntity upTe = level.getBlockEntity(startPosInnerWall.revert().relative(UP, 2).relative(dir, i));
                if(upTe instanceof ElectromagnetBE) {
                    electromagnets.put(new NCBlockPos(startPosInnerWall), (ElectromagnetBE) upTe);
                } else if(upTe instanceof RFAmplifierBE) {
                    amplifiers.put(new NCBlockPos(startPosInnerWall), (RFAmplifierBE) upTe);
                }
            }
            //outer
            for(int i = 0; i < steps+2; i++) {
                TileEntity te3 = level.getBlockEntity(startPosOuterWall.revert().relative(dir, i));
                if(te3 instanceof ElectromagnetBE) {
                    ElectromagnetBE magnet = (ElectromagnetBE) te3;
                    electromagnets.put(new NCBlockPos(startPosOuterWall), magnet);
                } else if(te3 instanceof RFAmplifierBE) {
                    amplifiers.put(new NCBlockPos(startPosOuterWall), (RFAmplifierBE) te3);
                }
                TileEntity te4 = level.getBlockEntity(startPosOuterWall.revert().relative(UP, 2).relative(dir, i));
                if(te4 instanceof ElectromagnetBE) {
                    electromagnets.put(new NCBlockPos(startPosOuterWall), (ElectromagnetBE) te4);
                } else if(te4 instanceof RFAmplifierBE) {
                    amplifiers.put(new NCBlockPos(startPosOuterWall), (RFAmplifierBE) te4);
                }
            }
        }
    }

    private void validateRing() {
        NCBlockPos pos = new NCBlockPos(controllerBE.getBlockPos().relative(UP));
        ringValid = true;
        for(Direction side: Arrays.asList(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+1;
            NCBlockPos startPosInnerWall = null;
            NCBlockPos startPosOuterWall = null;
            NCBlockPos startPosBottomWall = null;
            NCBlockPos startPosTopWall = null;
            World level = controllerBE.getLevel();
            //position to left corner of the ring
            switch (side) {
                case NORTH:
                    dir = EAST;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(NORTH, 2+shift).relative(WEST, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(UP));
                    break;

                case SOUTH:
                    dir = WEST;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(SOUTH, 2+shift).relative(EAST, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(UP));
                    break;

                case WEST:
                    dir = SOUTH;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(WEST, 2+shift).relative(NORTH, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(UP));
                    break;

                case EAST:
                    dir = NORTH;
                    startPosInnerWall = new NCBlockPos(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                    startPosOuterWall = new NCBlockPos(pos.revert().relative(EAST, 2+shift).relative(SOUTH, 1+shift));
                    startPosBottomWall = new NCBlockPos(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(DOWN));
                    startPosTopWall = new NCBlockPos(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(UP));
                    break;

            }
            //inner wall
            for(int i = 0; i < steps; i++) {
                TileEntity te = level.getBlockEntity(startPosInnerWall.revert().relative(dir, i));
                if(te instanceof FusionCasingBE) {
                    FusionCasingBE casing = (FusionCasingBE) te;
                    casing.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosInnerWall));
                    attachMultiblock(casing);
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosInnerWall);
                    return;
                }
            }
            //outer, bottom, top walls
            for(int i = 0; i < steps+2; i++) {
                TileEntity te = level.getBlockEntity(startPosOuterWall.revert().relative(dir, i));
                if(te instanceof FusionCasingBE ) {
                    FusionCasingBE casing = (FusionCasingBE) te;
                    casing.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosOuterWall));
                    attachMultiblock(casing);
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosOuterWall);
                    return;
                }
                TileEntity te2 = level.getBlockEntity(startPosBottomWall.revert().relative(dir, i));
                if(te2 instanceof FusionCasingBE) {
                    FusionCasingBE casing2 = (FusionCasingBE) te2;
                    casing2.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosBottomWall));
                    attachMultiblock(casing2);
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    controller().addErroredBlock(startPosBottomWall);
                    return;
                }
                TileEntity te3 = level.getBlockEntity(startPosTopWall.revert().relative(dir, i));
                if(te3 instanceof FusionCasingBE) {
                    FusionCasingBE casing3 = (FusionCasingBE) te3;
                    casing3.setController(controllerBE);
                    allBlocks.add(new NCBlockPos(startPosTopWall));
                    attachMultiblock(casing3);


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
            for(Direction side: Arrays.asList(NORTH, EAST, Direction.SOUTH, WEST)) {
                TileEntity te = controllerBE.getLevel().getBlockEntity(pos.revert().relative(side, i));
                if(te instanceof FusionConnectorBE) {
                    FusionConnectorBE connector = (FusionConnectorBE) te;
                    connector.setController(controllerBE);
                    attachMultiblock(connector);
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
        for(Direction side: Arrays.asList(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+2;
            NCBlockPos innerRingStartPos = null;
            World level = controllerBE.getLevel();
            //position to left corner of the ring
            switch (side) {
                case NORTH:
                    dir = EAST;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                break;
                case SOUTH:
                    dir = WEST;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                break;
                case WEST:
                    dir = SOUTH;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                break;
                case EAST:
                    dir = NORTH;
                    innerRingStartPos = new NCBlockPos(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                break;
            }
            for(int i = 0; i < steps; i++) {
                if(!processInnerBlock(innerRingStartPos.revert().relative(dir, i))) {
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
    protected boolean processInnerBlock(BlockPos toCheck) {
        return validInnerBlocks.contains(level().getBlockState(toCheck).getBlock());
    }

    private World level() {
        return controllerBE.getLevel();
    }

    @Override
    public void invalidateStats() {
        length = 0;
        controller().clearStats();
        isFormed = false;
        hasToRefresh = true;
    }

    @Override
    protected Direction getFacing() {
        return null;
    }

    public void tick() {
        super.tick();
        if(isFormed) {
            if(needToCollectFunctionalBlocks) {
                collectFunctionalParts();
                needToCollectFunctionalBlocks = false;
                needToRecalculateCharacteristics = true;
            } else if(needToRecalculateCharacteristics){
                recalculateCharacteristics();
                needToRecalculateCharacteristics = false;
            }
        }
    }

    public void recalculateCharacteristics() {
        magneticFieldStrength = 0;
        magnetsPower = 0;
        maxMagnetsTemp = 1000000;
        rfAmplification = 0;
        rfAmplifiersPower = 0;
        maxRFAmplifiersTemp = 1000000;
        double mEfficiency = 0;
        double rEfficiency = 0;
        for(ElectromagnetBE magnet: electromagnets.values()) {
            magneticFieldStrength += magnet.getStrength();
            mEfficiency += (int) magnet.getEfficiency();
            magnetsPower += magnet.getPower();
            if(magnet.getMaxTemperature() < maxMagnetsTemp) {
                maxMagnetsTemp = magnet.getMaxTemperature();
            }
        }
        magnetsEfficiency = (int) (mEfficiency / electromagnets.size());
        for(RFAmplifierBE amplifier: amplifiers.values()) {
            rfAmplification += amplifier.getAmplification();
            rfAmplifiersPower += amplifier.getPower();
            rEfficiency += (int) amplifier.getEfficiency();
            if(amplifier.getMaxTemperature() < maxRFAmplifiersTemp) {
                maxRFAmplifiersTemp = amplifier.getMaxTemperature();
            }
        }
        rfEfficiency = (int) (rEfficiency / amplifiers.size());
    }

    @Override
    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, pos, neighbor);
        if(!hasToRefresh && componentChanged(neighbor)) {
            needToCollectFunctionalBlocks = true;
        }
    }

    private boolean componentChanged(BlockPos neighbor) {
        //known component changed
        if(electromagnets.containsKey(neighbor) || amplifiers.containsKey(neighbor)) {
            return true;
        }
        TileEntity be = level().getBlockEntity(neighbor);
        //new added
        if(
                be instanceof ElectromagnetBE
                || be instanceof RFAmplifierBE
        ) {
            return true;
        }
        return false;
    }
}
