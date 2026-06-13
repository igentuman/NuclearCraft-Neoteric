package igentuman.nc.multiblock.particle_chamber;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.entity.ParticleChamberControllerBE;

public class ParticleChamberController implements MultiblockController {

    protected ParticleChamberControllerBE controllerBE;

    public ParticleChamberController(ParticleChamberControllerBE controllerBE) {
        this.controllerBE = controllerBE;
    }

    @Override
    public ParticleChamberControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE.isInternalValid = false;
        controllerBE.isCasingValid = false;
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (ParticleChamberControllerBE) multiblockControllerBE;
    }
}
