package igentuman.nc.multiblock.fission;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;

import static igentuman.nc.NuclearCraft.debugLog;

public class MSRController implements MultiblockController {

    protected MSRControllerBE controllerBE;

    public MSRController(MSRControllerBE msrControllerBE) {
        controllerBE = msrControllerBE;
    }

    @Override
    public MSRControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
        controllerBE().energyPerTick = 0;
        controllerBE().heatPerTick = 0;
        controllerBE().efficiency = 0;
        controllerBE().fuelCellsCount = 0;
        controllerBE().connectedPorts = 0;
        
        debugLog("MSR STATS CLEARED - All counts reset to 0");
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (MSRControllerBE) multiblockControllerBE;
    }
}
