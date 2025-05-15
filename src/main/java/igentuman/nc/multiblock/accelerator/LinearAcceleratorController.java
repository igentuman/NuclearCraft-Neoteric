package igentuman.nc.multiblock.accelerator;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.block.entity.fission.FissionControllerBE;

public class LinearAcceleratorController implements MultiblockController {

    protected final LinearAcceleratorControllerBE controllerBE;

    public LinearAcceleratorController(LinearAcceleratorControllerBE controller) {
        controllerBE = controller;
    }

    @Override
    public LinearAcceleratorControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
    }
}
