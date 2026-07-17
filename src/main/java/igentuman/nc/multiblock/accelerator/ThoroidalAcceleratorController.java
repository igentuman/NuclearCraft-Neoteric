package igentuman.nc.multiblock.accelerator;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;
import igentuman.nc.block.entity.MultiblockControllerBE;

public class ThoroidalAcceleratorController implements MultiblockController {

    protected RingAcceleratorControllerBE controllerBE;

    public ThoroidalAcceleratorController(RingAcceleratorControllerBE controller) {
        controllerBE = controller;
    }

    @Override
    public RingAcceleratorControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
        controllerBE().amplifiers = 0;
        controllerBE().coolers = 0;
        controllerBE().quadroupoles = 0;
        controllerBE().dipoles = 0;
        controllerBE().focus = 0;
        controllerBE().maxTemperature = 0;
        controllerBE().heatRate = 0;
        controllerBE().efficiency = 0;
        controllerBE().quadStrength = 0;
        controllerBE().dipoleStrength = 0;
        controllerBE().acceleratingVoltage = 0;
        controllerBE().energyRequired = 0;
        controllerBE().coolingRate = 0;
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (RingAcceleratorControllerBE)multiblockControllerBE;
    }
}
