package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.ElectromagnetBlock;
import igentuman.nc.block.RFAmplifierBlock;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.fusion.FusionConnectorBlock;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.FusionConfig.FUSION_CONFIG;
import static net.minecraft.core.Direction.*;
import static net.minecraft.world.level.block.Blocks.AIR;

public class FusionReactorMultiblock extends AbstractMultiblock {

    protected int magnetsEfficiency = 0;
    protected int rfEfficiency = 0;
    protected FusionCoreBE controllerBE;
    protected int length = 0;
    protected double magneticFieldStrength = 0;
    protected int magnetsPower = 0;
    protected int maxMagnetsTemp = 0;
    //KEV
    protected int rfAmplification = 0;
    protected int rfAmplifiersPower = 0;
    protected int maxRFAmplifiersTemp = 0;
    protected boolean connectorsValid = false;
    protected boolean ringValid = false;
    protected int connectorsCount = 0;
    protected int casingBlocks = 0;
    protected final HashMap<Long, ElectromagnetBlock> electromagnets = new HashMap<>();
    protected final HashMap<Long, RFAmplifierBlock> amplifiers = new HashMap<>();

    public FusionReactorMultiblock(FusionCoreBE core) {
        super(
                FusionReactorRegistration.CASING_BLOCKS, null,
                null, new HashSet<>(List.of(AIR)),
                new FusionReactorController(core));
        controllerBE = core;
        id = "fusion_reactor_"+controllerBE.getBlockPos().toShortString();
        MultiblockHandler.get(core.getLevel().dimension()).addMultiblock(this, true);
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
    public final HashSet<Block> validOuterBlocks() {
        return validOuterBlocks;
    }

    @Override
    public HashSet<Block> validInnerBlocks() {
        return validInnerBlocks;
    }

    protected FusionCoreBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (FusionCoreBE) controllerBe;
    }
    public boolean updateCharacteristics() {
        boolean hasChanges =
                controllerBE().magneticFieldStrength != magneticFieldStrength
                        || controllerBE().rfEfficiency != rfEfficiency
                        || controllerBE().magnetsEfficiency != magnetsEfficiency
                        || controllerBE().maxMagnetsTemp != maxMagnetsTemp
                        || controllerBE().rfAmplification != rfAmplification*controllerBE().rfAmplifierRatio()
                        || controllerBE().rfAmplifiersPower != rfAmplifiersPower*controllerBE().rfAmplifierRatio()
                        || controllerBE().minRFAmplifiersTemp != maxRFAmplifiersTemp;
        controllerBE().rfEfficiency = rfEfficiency;
        controllerBE().amplifiers = amplifiers.size();
        controllerBE().magnetsEfficiency = magnetsEfficiency;
        controllerBE().magneticFieldStrength = magneticFieldStrength;
        controllerBE().magnetsPower = magnetsPower;
        controllerBE().maxMagnetsTemp = maxMagnetsTemp;
        controllerBE().rfAmplification = (int) (rfAmplification*controllerBE().rfAmplifierRatio());
        controllerBE().rfAmplifiersPower = (int) (rfAmplifiersPower*controllerBE().rfAmplifierRatio());
        controllerBE().minRFAmplifiersTemp = maxRFAmplifiersTemp;
        controllerBE().connectors = connectorsCount;
        controllerBE().magnets = electromagnets.size();
        controllerBE().amplifiers = amplifiers.size();
        controllerBE().casingBlocks = casingBlocks;
        controllerBE().size = length;
        if(hasChanges) {
            controllerBE().currentRfAmplification = rfAmplification;
            controllerBE.setChanged();
        }
        return hasChanges;
    }

    @Override
    public void validateOuter() {
        debugLog("Starting fusion reactor outer validation");
        
        resolveDimensions();
        debugLog("Resolved fusion reactor dimensions - Length: " + length);
        
        if(!validationResult.isValid) {
            debugLog("Dimension resolution failed with result: " + validationResult);
            clearStats();
            return;
        }
        
        debugLog("Validating fusion reactor ring structure");
        validateRing();
        debugLog("Ring validation - Ring valid: " + ringValid + 
                ", Connectors valid: " + connectorsValid + 
                ", Connectors count: " + connectorsCount + 
                ", Casing blocks: " + casingBlocks);
        
        if(!validationResult.isValid) {
            debugLog("Ring validation failed with result: " + validationResult);
            clearStats();
            return;
        }
        
        outerValid = ringValid && connectorsValid;
        if(outerValid) {
            validationResult = ValidationResult.VALID;
            debugLog("Fusion reactor outer validation completed successfully");
        } else {
            debugLog("Fusion reactor outer validation failed - Ring valid: " + ringValid + ", Connectors valid: " + connectorsValid);
        }
    }

    @Override
    public void validate() {

        super.validate();
        
        debugLog("Handling potential meltdown conditions");
        handleMeltdown();
        
        debugLog("Collecting functional parts (electromagnets and RF amplifiers)");
        collectFunctionalParts();
        debugLog("Found " + electromagnets.size() + " electromagnets and " + amplifiers.size() + " RF amplifiers");
        
        debugLog("Recalculating fusion reactor characteristics");
        recalculateCharacteristics();
        debugLog("Magnetic field strength: " + String.format("%.2f", magneticFieldStrength) + 
                ", RF efficiency: " + rfEfficiency + 
                ", Magnets efficiency: " + magnetsEfficiency);
        
        boolean hasChanges = updateCharacteristics();
        debugLog("Updated controller characteristics - Changes detected: " + hasChanges);
        
        if(validationResult.isValid) {
            errorBlockPos = BlockPos.ZERO;
            debugLog("Fusion reactor validation completed successfully");
        } else {
            debugLog("Fusion reactor validation failed with result: " + validationResult);
        }
    }

    private void handleMeltdown() {
        if(!validationResult.isValid && controllerBE().plasmaTemperature > 100000) {
            controllerBE().plasmaTemperature = 0;
            getLevel().explode(null, errorBlockPos.getX(), errorBlockPos.getY(), errorBlockPos.getZ(),
                    2f, true, Level.ExplosionInteraction.TNT);
        }
    }

    private void processFunctionalBlock(BlockPosInstance pos)
    {
        if(getBlock(pos) instanceof ElectromagnetBlock magnet) {
            electromagnets.put(pos.asLong(), magnet);
            addIfNotExists(pos, allBlocks);
        } else if(getBlock(pos) instanceof RFAmplifierBlock amplifier) {
            amplifiers.put(pos.asLong(), amplifier);
            addIfNotExists(pos, allBlocks);
        }
    }

    public void collectFunctionalParts() {
        electromagnets.clear();
        amplifiers.clear();
        BlockPosInstance pos = new BlockPosInstance(controllerBE.getBlockPos());
        for(Direction side: List.of(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+1;
            BlockPosInstance startPosInnerWall = null;
            BlockPosInstance startPosOuterWall = null;
            //position to left corner of the ring
            switch (side) {
                case NORTH -> {
                    dir = EAST;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(NORTH, 2+shift).relative(WEST, 1+shift));
                }
                case SOUTH -> {
                    dir = WEST;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(SOUTH, 2+shift).relative(EAST, 1+shift));
                }
                case WEST -> {
                    dir = SOUTH;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(WEST, 2+shift).relative(NORTH, 1+shift));
                }
                case EAST -> {
                    dir = NORTH;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(EAST, 2+shift).relative(SOUTH, 1+shift));
                }
            }
            if(startPosInnerWall == null || startPosOuterWall == null) {
                return;
            }
            //inner
            for(int i = 0; i < steps; i++) {
                processFunctionalBlock(startPosInnerWall.revert().relative(dir, i));
                processFunctionalBlock(startPosInnerWall.revert().relative(UP, 2).relative(dir, i));
            }
            //outer
            for(int i = 0; i < steps+2; i++) {
                processFunctionalBlock(startPosOuterWall.revert().relative(dir, i));
                processFunctionalBlock(startPosOuterWall.revert().relative(UP, 2).relative(dir, i));
            }
        }
    }

    private void validateRing() {
        casingBlocks = 0;
        validationResult = ValidationResult.VALID;
        BlockPosInstance pos = new BlockPosInstance(controllerBE.getBlockPos().relative(UP));
        ringValid = true;
        for(Direction side: List.of(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+1;
            BlockPosInstance startPosInnerWall = null;
            BlockPosInstance startPosOuterWall = null;
            BlockPosInstance startPosBottomWall = null;
            BlockPosInstance startPosTopWall = null;
            //position to left corner of the ring
            switch (side) {
                case NORTH -> {
                    dir = EAST;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(NORTH, 2+shift).relative(WEST, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(NORTH, 1+shift).relative(WEST, 1+shift).relative(UP));
                }
                case SOUTH -> {
                    dir = WEST;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(SOUTH, 2+shift).relative(EAST, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(SOUTH, 1+shift).relative(EAST, 1+shift).relative(UP));
                }
                case WEST -> {
                    dir = SOUTH;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(WEST, 2+shift).relative(NORTH, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(WEST, 1+shift).relative(NORTH, 1+shift).relative(UP));
                }
                case EAST -> {
                    dir = NORTH;
                    startPosInnerWall = new BlockPosInstance(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                    startPosOuterWall = new BlockPosInstance(pos.revert().relative(EAST, 2+shift).relative(SOUTH, 1+shift));
                    startPosBottomWall = new BlockPosInstance(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(DOWN));
                    startPosTopWall = new BlockPosInstance(pos.revert().relative(EAST, 1+shift).relative(SOUTH, 1+shift).relative(UP));
                }
            }
            //inner wall
            for(int i = 0; i < steps; i++) {
                assert startPosInnerWall != null;
                if(isValidForOuter(startPosInnerWall.revert().relative(dir, i))) {
                    addIfNotExists(startPosInnerWall, allBlocks);
                    casingBlocks++;
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    if(casingBlocks > 0 || length > 1) {
                        errorBlockPos = new BlockPos(startPosInnerWall);
                    }
                    return;
                }
            }
            //outer, bottom, top walls
            for(int i = 0; i < steps+2; i++) {
                assert startPosOuterWall != null;
                if(isValidForOuter(startPosOuterWall.revert().relative(dir, i))) {
                    addIfNotExists(startPosOuterWall, allBlocks);
                    casingBlocks++;
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    errorBlockPos = new BlockPos(startPosOuterWall);
                    return;
                }
                assert startPosBottomWall != null;
                if(isValidForOuter(startPosBottomWall.revert().relative(dir, i))) {
                    addIfNotExists(startPosBottomWall, allBlocks);
                    casingBlocks++;
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    errorBlockPos = new BlockPos(startPosBottomWall);
                    return;
                }
                assert startPosTopWall != null;
                if(isValidForOuter(startPosTopWall.revert().relative(dir, i))) {
                    addIfNotExists(startPosTopWall, allBlocks);
                    casingBlocks++;
                } else {
                    ringValid = false;
                    validationResult = ValidationResult.WRONG_OUTER;
                    errorBlockPos = new BlockPos(startPosTopWall);
                    return;
                }
            }
        }
    }

    private void validateConnectors() {
        validationResult = ValidationResult.VALID;
        BlockPosInstance pos = new BlockPosInstance(controllerBE().getBlockPos().above());
        connectorsCount = 0;
        length = 1;
        connectorsValid = true;
        BlockPos possibleErrorPos = BlockPos.ZERO;
        for(int i = 2; i <= maxWidth()/2+1; i++) {
            int connectors = 0;
            for(Direction side: List.of(NORTH, EAST, Direction.SOUTH, Direction.WEST)) {
                if(getBlockState(pos.revert().relative(side, i), true).getBlock() instanceof FusionConnectorBlock) {
                    addIfNotExists(pos, allBlocks);
                    connectorsCount++;
                    connectors++;
                } else {
                    possibleErrorPos = new BlockPos(pos);
                }
            }
            if(connectors == 4) {
                length++;
            } else {
                if (connectors != 0) {
                    connectorsValid = false;
                    errorBlockPos = possibleErrorPos;
                    validationResult = ValidationResult.INCOMPLETE;
                } else {
                    connectorsValid = true;
                }
                return;
            }
        }
    }

    @Override
    public void validateInner() {
        if(!outerValid) return;
        BlockPosInstance pos = new BlockPosInstance(controllerBE.getBlockPos().relative(UP));
        innerValid = true;
        for(Direction side: List.of(NORTH, EAST, SOUTH, WEST)) {
            Direction dir = side;
            int steps = length*2+3;
            int shift = length+2;
            BlockPosInstance innerRingStartPos = null;
            Level level = controllerBE.getLevel();
            //position to left corner of the ring
            switch (side) {
                case NORTH -> {
                    dir = EAST;
                    innerRingStartPos = new BlockPosInstance(pos.revert().relative(NORTH, shift).relative(WEST, shift));
                }
                case SOUTH -> {
                    dir = WEST;
                    innerRingStartPos = new BlockPosInstance(pos.revert().relative(SOUTH, shift).relative(EAST, shift));
                }
                case WEST -> {
                    dir = SOUTH;
                    innerRingStartPos = new BlockPosInstance(pos.revert().relative(WEST, shift).relative(NORTH, shift));
                }
                case EAST -> {
                    dir = NORTH;
                    innerRingStartPos = new BlockPosInstance(pos.revert().relative(EAST, shift).relative(SOUTH, shift));
                }
            }
            for(int i = 0; i < steps; i++) {
                assert innerRingStartPos != null;
                if(!processInnerBlock(innerRingStartPos.revert().relative(dir, i))) {
                    innerValid = false;
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = new BlockPos(innerRingStartPos);
                    return;
                }
            }
        }
        validationResult =  ValidationResult.VALID;
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        return validInnerBlocks.contains(getBlockState(toCheck).getBlock());
    }

    @Override
    public void clearStats() {
        length = 0;
        controller().clearStats();
        isFormed = false;
    }

    @Override
    protected Direction getControllerDirection() {
        return UP;
    }

    @Override
    public void resolveDimensions()
    {
        validateConnectors();
        topRight = BlockPosInstance.of(validationResult.isValid ? controllerBE.getBlockPos().relative(UP, 2).relative(EAST, length+3).relative(SOUTH, length+3) : BlockPos.ZERO);
        bottomLeft = BlockPosInstance.of(validationResult.isValid ? controllerBE.getBlockPos().relative(WEST, length+3).relative(NORTH, length+3) : BlockPos.ZERO);
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
        for(ElectromagnetBlock magnet: electromagnets.values()) {
            magneticFieldStrength += magnet.getStrength();
            mEfficiency += (int) magnet.getEfficiency();
            magnetsPower += magnet.getPower();
            if(magnet.getMaxTemperature() < maxMagnetsTemp) {
                maxMagnetsTemp = magnet.getMaxTemperature();
            }
        }
        magnetsEfficiency = (int) (mEfficiency / electromagnets.size());
        for(RFAmplifierBlock amplifier: amplifiers.values()) {
            rfAmplification += amplifier.getAmplification();
            rfAmplifiersPower += amplifier.getPower();
            rEfficiency += (int) amplifier.getEfficiency();
            if(amplifier.getMaxTemperature() < maxRFAmplifiersTemp) {
                maxRFAmplifiersTemp = amplifier.getMaxTemperature();
            }
        }
        rfEfficiency = (int) (rEfficiency / amplifiers.size());
    }
}
