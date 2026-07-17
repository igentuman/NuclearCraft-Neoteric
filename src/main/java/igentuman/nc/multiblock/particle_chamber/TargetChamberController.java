package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;

public class TargetChamberController extends ParticleChamberController {

    public TargetChamberController(TargetChamberControllerBE controllerBE) {
        super(controllerBE);
    }

    @Override
    public TargetChamberControllerBE controllerBE() {
        return (TargetChamberControllerBE) controllerBE;
    }
}
