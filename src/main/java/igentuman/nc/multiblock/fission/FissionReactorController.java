package igentuman.nc.multiblock.fission;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;

import static igentuman.nc.NuclearCraft.debugLog;

public class FissionReactorController implements MultiblockController {

    protected FissionControllerBE controllerBE;

    public FissionReactorController(FissionControllerBE fissionControllerBE) {
        controllerBE = fissionControllerBE;
    }

    @Override
    public FissionControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        debugLog("CLEARING STATS - Previous values: FuelCells=" + controllerBE().fuelCellsCount +
                ", Moderators=" + controllerBE().moderatorsCount + ", HeatSinks=" + controllerBE().heatSinksCount + 
                ", InternalValid=" + controllerBE().isInternalValid + ", CasingValid=" + controllerBE().isCasingValid);
        
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
        controllerBE().moderatorAttachments = 0;
        controllerBE().moderatorsCount = 0;
        controllerBE().heatSinkCooling = 0;
        controllerBE().fuelCellsCount = 0;
        controllerBE().fuelCellMultiplier = 0;
        controllerBE().moderatorCellMultiplier = 0;
        controllerBE().irradiationLines = 0;
        controllerBE().cellsHeatMult = 0;
        controllerBE().cellsEnergyMult = 0;
        controllerBE().moderatorsEnergyMult = 0;
        controllerBE().moderatorsHeatMult = 0;
        
        debugLog("STATS CLEARED - All counts reset to 0");
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (FissionControllerBE) multiblockControllerBE;
    }
}
