package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.block.beam_diverter.entity.BeamDiverterControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.PortMode;
import igentuman.nc.block.ElectromagnetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_CASING_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class BeamDiverterMultiblock extends AbstractAcceleratorMultiblock {

    @Override public int maxHeight() { return 5; }
    @Override public int maxWidth()  { return 5; }
    @Override public int maxDepth()  { return 5; }
    @Override public int minHeight() { return 5; }
    @Override public int minWidth()  { return 5; }
    @Override public int minDepth()  { return 5; }

    private static HashSet<Block> getOuterBlocks() {
        HashSet<Block> blocks = getBlocksByTagKey(ACCELERATOR_CASING_BLOCKS.location().toString());
        blocks.add(ACCELERATOR_BLOCKS.get("beam_diverter_controller").get());
        blocks.add(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_beam_port").get());
        return blocks;
    }

    public BeamDiverterMultiblock(BeamDiverterControllerBE controllerBE) {
        super(
                getOuterBlocks(),
                getBlocksByTagKey(ACCELERATOR_INNER_BLOCKS.location().toString()),
                new BeamDiverterController(controllerBE)
        );
        id = "beam_diverter_" + controllerBE.getBlockPos().toShortString();
        controllerBe = controllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    protected BeamDiverterControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (BeamDiverterControllerBE) controllerBe;
    }

    @Override
    public void validateOuter() {
        outerValid = false;
        debugLog("Starting outer validation for multiblock at " + controllerPos.toShortString() + " (Type: " + getClass().getSimpleName() + ")");

        resolveDimensions();
        debugLog("Resolved dimensions: " + width + "x" + height + "x" + depth + " (WxHxD)");

        if (width > maxWidth() || height > maxHeight() || depth > maxDepth())
        {
            validationResult = ValidationResult.TOO_BIG;
            debugLog("Validation failed - TOO_BIG: " + width + "x" + height + "x" + depth + " exceeds max " + maxWidth() + "x" + maxHeight() + "x" + maxDepth());
            return;
        }

        if (width < minWidth() || height < minHeight() || depth < minDepth())
        {
            validationResult = ValidationResult.TOO_SMALL;
            debugLog("Validation failed - TOO_SMALL: " + width + "x" + height + "x" + depth + " below min " + minWidth() + "x" + minHeight() + "x" + minDepth());
            return;
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

        debugLog("Found " + connectedPorts + " connected ports");
        outerValid = true;
        validationResult = ValidationResult.VALID;
        debugLog("Outer validation completed successfully");
        updateAABB();
        if (!validationResult.isValid) {
            return;
        }
        if (height() != 5 || width() != 5 || depth() != 5) {
            validationResult = ValidationResult.WRONG_PROPORTIONS;
            outerValid = false;
        }
    }

    public Direction getControllerDirection()
    {
        return controllerBE().getFacing();
    }

    @Override
    public void validateInner() {
        if (!outerValid) {
            clearStats();
            return;
        }

        int inputCount = 0;
        int outputCount = 0;
        beamPortsBE.clear();
        beamPorts.clear();
        dipoleStrength = 0;
        int electromagnetsFE = 0;
        electromagnets.clear();

        List<Direction> horizontalDirs = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        BlockPos center = getCenterBlock();

        for (Direction dir : horizontalDirs) {
            BlockPos portPos = center.relative(dir, 2);
            BlockState bs = getBlockState(portPos);
            if (!bs.is(ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())) {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = portPos;
                clearStats();
                return;
            }
            BlockEntity be = getBlockEntity(portPos);
            if (!(be instanceof AcceleratorBeamPortBE portBE)) {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = portPos;
                clearStats();
                return;
            }
            if (portBE.isInput()) {
                inputCount++;
            } else if (portBE.isOutput()) {
                outputCount++;
            }
            beamPortsBE.put(portPos.asLong(), portBE);
            beamPorts.add(portPos.asLong());
        }

        if (inputCount != 1) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = center;
            clearStats();
            return;
        }
        if (outputCount < 1) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = center;
            clearStats();
            return;
        }

        Block beamBlock = ACCELERATOR_BLOCKS.get("particle_beam").get();
        Block yokeBlock = ACCELERATOR_BLOCKS.get("electromagnet_yoke").get();

        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 3; x++) {
                for (int z = 1; z <= 3; z++) {
                    BlockPos pos = ((BlockPosInstance)bottomLeft).revert().offset(x, y, z);
                    addIfNotExists(pos, allBlocks);
                    attachMultiblock(pos);

                    BlockState bs = getBlockState(pos);
                    int dx = pos.getX() - center.getX();
                    int dy = pos.getY() - center.getY();
                    int dz = pos.getZ() - center.getZ();

                    if (dy == 0 && (dx == 0 || dz == 0) && Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                        if (!bs.is(beamBlock)) {
                            validationResult = ValidationResult.WRONG_INNER;
                            errorBlockPos = pos;
                            clearStats();
                            return;
                        }
                    } else if (dx == 0 && dz == 0 && Math.abs(dy) == 1) {
                        if (!(bs.getBlock() instanceof ElectromagnetBlock)) {
                            validationResult = ValidationResult.WRONG_INNER;
                            errorBlockPos = pos;
                            clearStats();
                            return;
                        } {
                            electromagnets.putIfAbsent(pos.asLong(), (ElectromagnetBlock) bs.getBlock());
                            dipoleStrength = ((ElectromagnetBlock) bs.getBlock()).getStrength();
                            electromagnetsFE += ((ElectromagnetBlock) bs.getBlock()).getPower();
                        }
                    } else {
                        if (!bs.is(yokeBlock)) {
                            validationResult = ValidationResult.WRONG_INNER;
                            errorBlockPos = pos;
                            clearStats();
                            return;
                        }
                    }
                }
            }
        }

        validationResult = ValidationResult.VALID;
        innerValid = true;
        ((BlockPosInstance) bottomLeft).revert();
        BeamDiverterControllerBE ctrl = controllerBE();
        ctrl.connectedPorts = beamPorts.size();
        ctrl.height = height;
        ctrl.width = width;
        ctrl.depth = depth;
        ctrl.efficiency = 100D;
        ctrl.energyPerTick = ACCELERATOR_CONFIG.BASE_ENERGY_REQUIREMENT.get() + electromagnetsFE;
        ctrl.dipoleStrength = dipoleStrength;
        ctrl.refresh();
    }

    @Override
    public void clearStats() {
        super.clearStats();
        beamPorts.clear();
    }

    public void extractParticle(ParticleStack outputParticle) {
        for (long pos : beamPortsBE.keySet()) {
            if (getBlockState(pos).getValue(PORT_MODE) == PortMode.Mode.OUTPUT) {
                BlockEntity be = beamPortsBE.get(pos);
                if (be instanceof AcceleratorBeamPortBE port) {
                    if(port.extractParticle(outputParticle)) {
                        break;
                    }
                }
            }
        }
    }

    public AcceleratorBeamPortBE getInputBeamPort() {
        for (long pos : beamPortsBE.keySet()) {
            if (getBlockState(pos).getValue(PORT_MODE) == PortMode.Mode.INPUT) {
                BlockEntity be = beamPortsBE.get(pos);
                if (be instanceof AcceleratorBeamPortBE port) {
                    return port;
                }
            }
        }
        return null;
    }

    public AcceleratorBeamPortBE getFirstOutputBeamPort() {
        for (long pos : beamPortsBE.keySet()) {
            if (getLevel().getBlockState(BlockPos.of(pos)).getValue(PORT_MODE) == PortMode.Mode.OUTPUT) {
                BlockEntity be = beamPortsBE.get(pos);
                if (be instanceof AcceleratorBeamPortBE port) {
                    return port;
                }
            }
        }
        return null;
    }
}
