package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.*;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.collection.HashList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static igentuman.nc.handler.config.CommonConfig.TURBINE_CONFIG;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.INNER_TURBINE_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TurbineMultiblock extends AbstractNCMultiblock {
    public Direction turbineDirection;
    public List<BlockPos> bearingPositions = new ArrayList<>();
    public List<BlockPos> rotorPositions = new ArrayList<>();
    public List<BlockPos> coilPositions = new ArrayList<>();
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

    public void validateInner() {
        super.validateInner();
    }

    @Override
    public void validate()
    {
        coilPositions.clear();
        rotorPositions.clear();
        bearingPositions.clear();
        bladePositions.clear();
        super.validate();
        if(isFormed) {
            detectOrientation();
        }
    }

    private void detectOrientation() {
        BlockPos rotorPos = rotorPositions.get(0);
        BlockState st = getLevel().getBlockState(rotorPos);
        turbineDirection = st.getValue(TurbineRotorBlock.FACING);
    }

    @Override
    public void tick() {
        super.tick();
        TurbineControllerBE<?> controller = (TurbineControllerBE<?>) controller().controllerBE();
        controller.updateEnergyStorage();
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        BlockState bs = getLevel().getBlockState(toCheck);
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
        BlockState bs = getLevel().getBlockState(pos);
        if(bs.getBlock().asItem().toString().contains("bearing")) {
            bearingPositions.add(new NCBlockPos(pos));
        }
        if(bs.getBlock().asItem().toString().contains("coil")) {
            coilPositions.add(new NCBlockPos(pos));
        }
    }


    public void invalidateStats()
    {
        controller().clearStats();
    }

    protected Direction getFacing() {
        return ((TurbineControllerBE<?>)controller().controllerBE()).getFacing();
    }

}
