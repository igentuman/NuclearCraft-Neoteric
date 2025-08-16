package igentuman.nc.multiblock.particle_chamber;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;

public class TargetChamberController implements MultiblockController {

    protected TargetChamberControllerBE controllerBE;

    public TargetChamberController(TargetChamberControllerBE TargetChamberControllerBE) {
        controllerBE = TargetChamberControllerBE;
    }

    @Override
    public TargetChamberControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (TargetChamberControllerBE) multiblockControllerBE;
    }
}
