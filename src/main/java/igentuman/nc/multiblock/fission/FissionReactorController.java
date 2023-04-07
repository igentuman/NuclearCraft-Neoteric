package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.multiblock.INCMultiblockController;

public class FissionReactorController implements INCMultiblockController {
    protected FissionControllerBE controllerBE;
    public FissionReactorController(FissionControllerBE fissionControllerBE) {
        controllerBE = fissionControllerBE;
    }

    @Override
    public FissionControllerBE controllerBE() {
        return controllerBE;
    }
}
