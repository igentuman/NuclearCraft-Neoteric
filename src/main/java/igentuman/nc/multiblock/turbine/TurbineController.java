package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.multiblock.INCMultiblockController;
import net.minecraft.core.BlockPos;

public class TurbineController implements INCMultiblockController {
    protected TurbineControllerBE<?> controllerBE;
    public TurbineController(TurbineControllerBE<?> fissionControllerBE) {
        controllerBE = fissionControllerBE;
    }

    @Override
    public TurbineControllerBE<?> controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
    }

    @Override
    public void addErroredBlock(BlockPos relative) {
        controllerBE().errorBlockPos = relative;
    }
}
