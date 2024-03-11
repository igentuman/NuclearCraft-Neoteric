package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.*;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TurbineMultiblock extends AbstractNCMultiblock {
    public Direction turbineDirection;
    public boolean isRotorValid = false;
    public List<BlockPos> bearingPositions = new ArrayList<>();
    public List<BlockPos> rotorPositions = new ArrayList<>();
    public List<BlockPos> coilPositions = new ArrayList<>();
    public int flow = 0;
    private List<BlockPos> bladePositions = new ArrayList<>();

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

    public TurbineMultiblock(TurbineControllerBE<?> turbineControllerBE) {
        super(
                getBlocksByTagKey(CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(INNER_TURBINE_BLOCKS.location().toString())
        );
        controller = new TurbineController(turbineControllerBE);
    }

    public List<Block> validCornerBlocks() {
        return List.of(TURBINE_BLOCKS.get("turbine_casing").get());
    }

    public void validateInner() {
        if(!outerValid) return;
        super.validateInner();
        detectOrientation();
        isRotorValid = validateRotor();
        if(!isRotorValid) {
            validationResult =  ValidationResult.WRONG_INNER;
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
        if(!validateProportions()) {
            validationResult = ValidationResult.WRONG_PROPORTIONS;
            innerValid = false;
            outerValid = false;
            isFormed = false;
        } else {
            countCoils();
            countBlades();
        }
    }

    private void countBlades() {
        flow = 0;
        for(BlockPos pos : bladePositions) {
            BlockEntity be = getBlockEntity(pos);
            if(be instanceof TurbineBladeBE blade) {
                flow++;
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
    public void tick() {
        super.tick();
        TurbineControllerBE<?> controller = (TurbineControllerBE<?>) controller().controllerBE();
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        BlockState bs = getBlockState(toCheck);
        if(bs.isAir()) return true;
        super.processInnerBlock(new NCBlockPos(toCheck));
        if(bs.getBlock() instanceof TurbineRotorBlock) {
            rotorPositions.add(new NCBlockPos(toCheck));
        }
        if(bs.getBlock() instanceof TurbineBladeBlock) {
            bladePositions.add(new NCBlockPos(toCheck));
        }
        return true;
    }

    protected void processOuterBlock(BlockPos pos) {
        super.processOuterBlock(pos);
        BlockEntity bs = getBlockEntity(pos);
        if(bs instanceof TurbineBearingBE) {
            bearingPositions.add(new NCBlockPos(pos));
        }
        if(bs instanceof TurbineCoilBE) {
            coilPositions.add(new NCBlockPos(pos));
        }
    }

    public int activeCoils = 0;
    public double coilsEfficiency = 0;

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
                if(getFacing().getAxis().equals(Direction.Axis.Z)) {
                    return height() == width() && height() % 2 != 0;
                }
                return depth() == height() && height() % 2 != 0;
            case EAST:
            case WEST:
                if(getFacing().getAxis().equals(Direction.Axis.X)) {
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


    public void invalidateStats()
    {
        controller().clearStats();
    }

    protected Direction getFacing() {
        return ((TurbineControllerBE<?>)controller().controllerBE()).getFacing();
    }

}
