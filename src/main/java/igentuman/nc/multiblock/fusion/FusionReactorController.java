package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.api.nc.multiblock.MultiblockController;

public class FusionReactorController implements MultiblockController {

    protected FusionCoreBE controllerBE;
    public FusionReactorController(FusionCoreBE FusionCoreBE) {
        controllerBE = FusionCoreBE;
    }

    @Override
    public FusionCoreBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().reactorHeat = 0;
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (FusionCoreBE)multiblockControllerBE;
    }
}
