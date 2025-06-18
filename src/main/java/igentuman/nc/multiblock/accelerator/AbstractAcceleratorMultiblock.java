package igentuman.nc.multiblock.accelerator;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.ElectromagnetBlock;
import igentuman.nc.block.RFAmplifierBlock;
import igentuman.nc.block.accelerator.CoolerBlock;
import igentuman.nc.block.entity.accelerator.AcceleratorBeamPortBE;
import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.PortMode;
import igentuman.nc.util.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class AbstractAcceleratorMultiblock extends AbstractMultiblock {

    protected static final int FINAL_STAGE = 4;
    protected BlockPosInstance centerPos;
    protected BlockPos ionSourcePos = BlockPos.ZERO;
    protected BlockPos outputBeamPort = BlockPos.ZERO;
    protected final HashMap<Long, ElectromagnetBlock> electromagnets = new HashMap<>(1000);
    protected final HashMap<Long, RFAmplifierBlock> amplifiers = new HashMap<>(1000);
    protected final HashMap<Long, CoolerBlock> coolers = new HashMap<>(1000);
    protected HashSet<Long> beamPorts = new HashSet<>();
    protected int dipolesCount = 0;
    protected int quadrupolesCount = 0;
    protected final int[] yCoords = new int[]{-1, 1, 0, 0, 1, 1, 1, -1};
    protected final int[] xCoords = new int[]{0, 0, -1, 1, 1, 1, -1, 1};
    protected double focus = 0.0;
    protected int maxTemperature = 0;
    protected int heatRate = 0;
    protected double efficiency = 0.0;
    protected double quadStrength = 0.0;
    protected double dipoleStrength = 0.0;
    protected long acceleratingVoltage = 0;
    protected int energyRequired = 0;
    protected int coolingRate = 0;
    protected int validCoolers = 0;
    protected int stage = 0;

    protected AbstractAcceleratorMultiblock(HashSet<Block> validOuterBlocks, HashSet<Block> validInnerBlocks, MultiblockController controller) {
        super(validOuterBlocks, validInnerBlocks, controller);
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
        return maxDepth();
    }
    @Override
    public int minWidth() {
        return minDepth();
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


    @Override
    protected Direction getControllerDirection() {
        return Direction.NORTH;
    }

    @Override
    public void clearStats() {
        controller().clearStats();
    }

    public boolean isControllerPlacedOnSide() {
       return depth == 5;
    }

    @Override
    public boolean isValidCorner(BlockPos pos)
    {
        try {
            return getBlockState(pos).is(ACCELERATOR_BLOCKS.get("accelerator_casing").get());
        } catch (NullPointerException ignored) { }
        return false;
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
        beamPorts.clear();
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
        if(bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())) {
            beamPorts.add(centerPos.asLong());
        }
        ionSourcePos = bs.is(ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get()) ? new BlockPosInstance(centerPos) : BlockPos.ZERO;
        outputBeamPort = bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get()) ? new BlockPosInstance(centerPos) : BlockPos.ZERO;
        bs = getBlockState(centerPos.offset(0,0, maxZ-minZ));
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
        outputBeamPort = bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get()) && outputBeamPort.equals(BlockPos.ZERO) ? new BlockPosInstance(centerPos) : BlockPos.ZERO;
        if(bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())) {
            beamPorts.add(centerPos.asLong());
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
        for (int z = 1; z < depth-1; z++) {
            if(!indexSlice(z)) {
                return;
            }
        }

        innerValid = true;
        validationResult =  ValidationResult.VALID;
        stage = 3;
    }

    protected boolean indexSlice(int z) {
        //first of all it checks center blocks and then corner blocks
        int magnetCount = 0;
        boolean nextMustBeMagnet = false;
        boolean nextMustBeAmplifier = false;
        double magnetStrength = 0;
        for(int i = 0; i < xCoords.length; i++ ) {
            int x = xCoords[i];
            int y = yCoords[i];
            BlockPos toCheck = new BlockPos(getSidePos(x).above(y).relative(multiblockDirection, z));

            BlockState bs = getBlockState(toCheck);
            addIfNotExists(toCheck, allBlocks);
            if (!isValidForInner(bs)) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = new BlockPos(toCheck);
                return false;
            }
            if(isMagnet(bs)) {
                if((i > 4 || nextMustBeAmplifier)) {
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = new BlockPos(toCheck);
                    return false;
                }
                electromagnets.put(toCheck.asLong(), (ElectromagnetBlock) bs.getBlock());
                maxTemperature = Math.min(maxTemperature, ((ElectromagnetBlock) bs.getBlock()).getMaxTemperature());
                heatRate+= ((ElectromagnetBlock) bs.getBlock()).getHeatRate();
                energyRequired+= ((ElectromagnetBlock) bs.getBlock()).getPower();
                efficiency+= ((ElectromagnetBlock) bs.getBlock()).getEfficiency();
                magnetStrength+= ((ElectromagnetBlock) bs.getBlock()).getStrength();
                magnetCount++;
                nextMustBeMagnet = magnetCount % 2 != 0 && (i == 0 || i == 2);
            }
            if(isAmplifier(bs)) {
                if(nextMustBeMagnet) {
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = new BlockPos(toCheck);
                    return false;
                }
                acceleratingVoltage+= ((RFAmplifierBlock) bs.getBlock()).getAmplification();
                energyRequired+= ((RFAmplifierBlock) bs.getBlock()).getPower();
                heatRate+= ((RFAmplifierBlock) bs.getBlock()).getHeatRate();
                efficiency+= ((RFAmplifierBlock) bs.getBlock()).getEfficiency();
                maxTemperature = Math.min(maxTemperature, ((RFAmplifierBlock) bs.getBlock()).getMaxTemperature());
                nextMustBeAmplifier = true;
                amplifiers.put(toCheck.asLong(), (RFAmplifierBlock) bs.getBlock());
                continue;
            }
            if(isCooler(bs)) {
                coolers.put(toCheck.asLong(), (CoolerBlock) bs.getBlock());
                if(nextMustBeMagnet || nextMustBeAmplifier) {
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = new BlockPos(toCheck);
                    return false;
                }
                continue;
            }
            if(nextMustBeAmplifier) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = new BlockPos(toCheck);
                return false;
            }

            if(i == 1 && magnetCount == 1) {
                validationResult = ValidationResult.WRONG_INNER;
                int yy = isMagnet(bs) ? 2 : 0;
                errorBlockPos = new BlockPos(toCheck.below(yy));
                return false;
            }
            if(i == 3 && (magnetCount == 3 || magnetCount == 1)) {
                validationResult = ValidationResult.WRONG_INNER;
                int xx = isMagnet(bs) ? 2 : i;
                errorBlockPos = new BlockPos(new BlockPos(getSidePos(leftCasing - xCoords[xx]).above(y - bottomCasing).relative(multiblockDirection, z)));
                return false;
            }
        }
        switch (magnetCount) {
            case 4 -> {
                quadrupolesCount++;
                quadStrength += magnetStrength;
            }
            case 2 -> {
                dipolesCount++;
                dipoleStrength += magnetStrength;
            }
        }
        return true;
    }

    protected boolean isCooler(BlockState bs) {
        return bs.getBlock() instanceof CoolerBlock;
    }

    protected boolean isAmplifier(BlockState bs) {
        return bs.getBlock() instanceof RFAmplifierBlock;
    }

    protected boolean isMagnet(BlockState bs) {
        return bs.getBlock() instanceof ElectromagnetBlock;
    }

    public void tick() {
        if(!canTick || !hasToRefresh) return;

        canTick = false;
        validationResult = ValidationResult.INCOMPLETE;
        if(stage == 0) {
            innerValid = false;
            outerValid = false;
            isFormed = false;
        }
        hasToRefresh = false;
        validate();
        canTick = true;
    }

    protected void indexCoolers() {
        innerValid = true;
        coolingRate = 0;
        validCoolers = 0;
        validationResult =  ValidationResult.VALID;
        stage = FINAL_STAGE;
        for (Long pos : coolers.keySet()) {
            CoolerBlock cooler = coolers.get(pos);
            if(cooler.isValid(getLevel(), BlockPos.of(pos), this)) {
                coolingRate += cooler.def.heat;
                validCoolers++;
            }
        }
    }

    protected void validateBeam() {
        stage = FINAL_STAGE;
        for(int i = 1; i < Math.max(depth, width)-1; i++) {
            if(!getBlockState(centerPos.revert().relative(multiblockDirection, i)).is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                validationResult = ValidationResult.WRONG_INNER;
                errorBlockPos = new BlockPosInstance(centerPos);
                return;
            }
        }
        innerValid = true;
        validationResult =  ValidationResult.VALID;
        centerPos.revert();
        stage = 2;
    }

    protected boolean processInnerBlock(BlockPos pos) {
        Block block = getBlock(pos);
        if(block instanceof ElectromagnetBlock magnet) {
            electromagnets.put(pos.asLong(), magnet);
        } else if(block instanceof RFAmplifierBlock amplifier) {
            amplifiers.put(pos.asLong(), amplifier);
        } else if(block instanceof CoolerBlock cooler) {
            coolers.put(pos.asLong(), cooler);
        }
        addIfNotExists(pos, allBlocks);
        return true;
    }

    public void extractParticle(ParticleStack particleStack) {
        for (long pos : beamPorts) {
            BlockPosInstance portPos = BlockPosInstance.of(pos);
            BlockState bs = getBlockState(portPos);
            if (!bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())) continue;
            if(bs.getValue(PORT_MODE).equals(PortMode.Mode.OUTPUT)) {
                BlockEntity be = getBlockEntity(portPos);
                if(be instanceof AcceleratorBeamPortBE port) {
                    port.extractParticle(particleStack);
                }
            }
        }
    }
}
