package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.api.nc.MultiblockController;
import net.minecraft.core.BlockPos;

public class FusionReactorController implements MultiblockController {
    protected FusionCoreBE<?> controllerBE;
    public FusionReactorController(FusionCoreBE<?> FusionCoreBE) {
        controllerBE = FusionCoreBE;
    }

    @Override
    public FusionCoreBE<?> controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().errorBlockPos = null;
        controllerBE().isCasingValid = false;
        controllerBE().plasmaTemperature = 0;
        controllerBE().reactorHeat = 0;
    }

    @Override
    public void addErroredBlock(BlockPos relative) {
        controllerBE().errorBlockPos = relative;
    }
}
