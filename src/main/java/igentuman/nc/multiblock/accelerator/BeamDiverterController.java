package igentuman.nc.multiblock.accelerator;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.block.beam_diverter.entity.BeamDiverterControllerBE;
import igentuman.nc.block.entity.MultiblockControllerBE;

public class BeamDiverterController implements MultiblockController {

    protected BeamDiverterControllerBE controllerBE;

    public BeamDiverterController(BeamDiverterControllerBE controller) {
        controllerBE = controller;
    }

    @Override
    public BeamDiverterControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
/*        controllerBE().focus = 0;
        controllerBE().dipoleStrength = 0;
        controllerBE().energyRequired = 0;*/
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (BeamDiverterControllerBE) multiblockControllerBE;
    }
}
