package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.turbine.TurbineBearingBlock;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.block.turbine.entity.TurbineBladeBE;
import igentuman.nc.block.turbine.entity.TurbineCoilBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.block.turbine.entity.TurbineRotorBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.*;

public class TurbineMultiblock extends AbstractMultiblock {

    public Direction turbineDirection;
    public boolean isRotorValid = false;
    public final List<BlockPos> bearingPositions = new ArrayList<>();
    public final List<BlockPos> rotorPositions = new ArrayList<>();
    public final HashSet<BlockPos> coilPositions = new HashSet<>();
    public float flow = 0;
    public int activeCoils = 0;
    public double coilsEfficiency = 0;
    public int blades = 0;
    private final HashSet<BlockPos> bladePositions = new HashSet<>();

    @Override
    public int maxHeight() {
        return TURBINE_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxWidth() {
        return TURBINE_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxDepth() {
        return TURBINE_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minHeight() {
        return TURBINE_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minWidth() {return TURBINE_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minDepth() { return TURBINE_CONFIG.MIN_SIZE.get(); }

    public TurbineMultiblock(TurbineControllerBE turbineControllerBE) {
        super(
                CASING_BLOCKS, null,
                INNER_TURBINE_BLOCKS, null,
                new TurbineController(turbineControllerBE)
        );
        id = "turbine_"+turbineControllerBE.getBlockPos().toShortString();
        MultiblockHandler.get(turbineControllerBE.getLevel().dimension()).addMultiblock(this);
    }

    public HashSet<Block> validCornerBlocks() {
        return new HashSet<>(List.of(TURBINE_BLOCKS.get("turbine_casing").get()));
    }

    public void validateInner() {
        if(!outerValid) {
            debugLog("Skipping inner validation - outer validation failed");
            return;
        }
        
        debugLog("Starting turbine inner validation");
        super.validateInner();
        
        debugLog("Detecting turbine orientation");
        detectOrientation();
        debugLog("Turbine direction: " + (turbineDirection != null ? turbineDirection.getName() : "null"));
        
        debugLog("Validating rotor configuration");
        isRotorValid = validateRotor();
        debugLog("Rotor validation result: " + isRotorValid + 
                ", Bearings: " + bearingPositions.size() + 
                ", Rotors: " + rotorPositions.size() + 
                ", Blades: " + blades);
        
        if(!isRotorValid) {
            debugLog("Rotor validation failed - setting result to WRONG_INNER");
            validationResult = ValidationResult.WRONG_INNER;
        }
    }

    @Override
    public void validate()
    {
        coilPositions.clear();
        rotorPositions.clear();
        bearingPositions.clear();
        bladePositions.clear();

        super.validate();
        
        if(!validationResult.isValid) {
            debugLog("Turbine validation failed with result: " + validationResult);
            clearStats();
            return;
        }
        if(!validateProportions()) {
            validationResult = ValidationResult.WRONG_PROPORTIONS;
            innerValid = false;
            outerValid = false;
            isFormed = false;
            clearStats();
            return;
        } else {
            countCoils();
            countBlades();
            if(blades % 4 != 0) {
                validationResult = ValidationResult.WRONG_BLADES;
                innerValid = false;
                outerValid = false;
                isFormed = false;
                clearStats();
                return;
            }
        }
        controllerBE().topRight = topRight;
        controllerBE().bottomLeft = bottomLeft;
        controllerBE().orientation = turbineDirection;
        controllerBE().coilsEfficiency = coilsEfficiency;
        controllerBE().activeCoils = activeCoils;
        controllerBE().blades = blades;
        controllerBE().flow = flow;
        controllerBE().bearingPos = bearingPositions.get(0);
        controllerBE().refresh();
    }

    @Override
    public TurbineController controller() {
        return (TurbineController) controller;
    }

    @Override
    protected TurbineControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (TurbineControllerBE) controllerBe;
    }

    private void countBlades() {
        flow = 0;
        blades = 0;
        for(BlockPos pos : bladePositions) {
            BlockEntity be = getBlockEntity(pos);
            if(be instanceof TurbineBladeBE blade) {
                if(blade.isValid()) {
                    flow += blade.getFlow();
                    blades++;
                }
            }
        }
    }

    private void detectOrientation() {
        if(rotorPositions.isEmpty()) return;
        BlockPos rotorPos = rotorPositions.get(0);
        BlockState st = getBlockState(rotorPos);
        turbineDirection = st.getValue(TurbineRotorBlock.FACING);
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        BlockState bs = getBlockState(toCheck);
        if(bs.isAir()) return true;
        super.processInnerBlock(new BlockPosInstance(toCheck));
        if(bs.getBlock() instanceof TurbineRotorBlock) {
            rotorPositions.add(new BlockPosInstance(toCheck));
        }
        if(bs.getBlock() instanceof TurbineBladeBlock) {
            bladePositions.add(new BlockPosInstance(toCheck));
        }
        return true;
    }

    protected void processOuterBlock(BlockPos pos) {
        super.processOuterBlock(pos);
        BlockEntity be = getBlockEntity(pos);
        BlockState bs = getBlockState(pos);
        if(bs.getBlock() instanceof TurbineBearingBlock) {
            bearingPositions.add(new BlockPosInstance(pos));
        }
        if(be instanceof TurbineCoilBE) {
            coilPositions.add(new BlockPosInstance(pos));
        }
    }

    public void countCoils() {
        activeCoils = 0;
        coilsEfficiency = 0;
        for(BlockPos pos : coilPositions) {
            BlockEntity be = getBlockEntity(pos);
            if(be instanceof TurbineCoilBE coil) {
                coil.validatePlacement();
                if(coilsEfficiency == 0) {
                    coilsEfficiency = coil.getRealEfficiency();
                }
                coilsEfficiency = (coilsEfficiency+coil.getRealEfficiency())/2;
                activeCoils += coil.isValid() ? 1 : 0;
            }
        }
    }

    public boolean validateProportions()
    {
        if(turbineDirection == null || bearingPositions.size() != 2) return false;
        switch (turbineDirection) {
            case UP:
            case DOWN:
                return width() == depth() && width() % 2 != 0;
            case NORTH:
            case SOUTH:
                if(getControllerDirection().getAxis().equals(Direction.Axis.Z)) {
                    return height() == width() && height() % 2 != 0;
                }
                return depth() == height() && height() % 2 != 0;
            case EAST:
            case WEST:
                if(getControllerDirection().getAxis().equals(Direction.Axis.X)) {
                    return height() == width() && height() % 2 != 0;
                }
                return height() == depth() && height() % 2 != 0;
        }
        return false;
    }

    public boolean validateRotor() {
        if(rotorPositions.isEmpty()) return false;
        boolean bearingConnected = true;
        Direction dir = turbineDirection;
        for(BlockPos pos : rotorPositions) {
            BlockState bs = getBlockState(pos);
            if(!(bs.getBlock() instanceof TurbineRotorBlock)) {
                return false;
            }
            if(bs.getValue(TurbineRotorBlock.FACING) != dir) {
                return false;
            }
            switch (dir) {
                case UP:
                case DOWN:
                    if(pos.getZ() != rotorPositions.get(0).getZ()
                    || pos.getX() != rotorPositions.get(0).getX()) {
                        return false;
                    }
                    break;
                case NORTH:
                case SOUTH:
                    if(pos.getY() != rotorPositions.get(0).getY()
                    || pos.getX() != rotorPositions.get(0).getX()) {
                        return false;
                    }
                    break;
                case EAST:
                case WEST:
                    if(pos.getY() != rotorPositions.get(0).getY()
                    || pos.getZ() != rotorPositions.get(0).getZ()) {
                        return false;
                    }
                    break;
            }
            BlockEntity be = getBlockEntity(pos);
            if(!(be instanceof TurbineRotorBE rotorBE)) {
                return false;
            }
            rotorBE.updateBearingConnection();
            bearingConnected = bearingConnected && rotorBE.connectedToBearing;
        }
        return bearingConnected && getBlockEntity(getCenterBlock()) instanceof TurbineRotorBE;
    }

    public void clearStats()
    {
        controller().clearStats();
    }

    protected Direction getControllerDirection() {
        return controller().controllerBE().getFacing();
    }
}
