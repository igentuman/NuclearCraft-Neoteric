package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.api.nc.multiblock.MultiblockController;

public class TurbineController implements MultiblockController {

    protected final TurbineControllerBE controllerBE;
    public TurbineController(TurbineControllerBE turbineControllerBE) {
        controllerBE = turbineControllerBE;
    }

    @Override
    public TurbineControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
    }
}
