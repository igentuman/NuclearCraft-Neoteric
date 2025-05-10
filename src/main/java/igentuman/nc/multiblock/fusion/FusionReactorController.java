package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.api.nc.multiblock.MultiblockController;

public class FusionReactorController implements MultiblockController {

    protected final FusionCoreBE controllerBE;
    public FusionReactorController(FusionCoreBE FusionCoreBE) {
        controllerBE = FusionCoreBE;
    }

    @Override
    public FusionCoreBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().errorBlockPos = null;
        controllerBE().isCasingValid = false;
        controllerBE().isInternalValid = false;
        controllerBE().plasmaTemperature = 0;
        controllerBE().reactorHeat = 0;
    }
}
