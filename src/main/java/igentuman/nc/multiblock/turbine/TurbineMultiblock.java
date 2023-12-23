package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.*;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import static igentuman.nc.handler.config.CommonConfig.TURBINE_CONFIG;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.INNER_TURBINE_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TurbineMultiblock extends AbstractNCMultiblock {

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

    public void validateInner()
    {
        super.validateInner();

        TurbineControllerBE<?> controller = (TurbineControllerBE<?>) controller().controllerBE();

        controller.updateEnergyStorage();
    }

    @Override
    protected boolean validateInnerBlock(BlockPos toCheck) {

        return true;
    }


    public void invalidateStats()
    {
        controller().clearStats();
    }

    protected Direction getFacing() {
        return ((TurbineControllerBE<?>)controller().controllerBE()).getFacing();
    }

}
