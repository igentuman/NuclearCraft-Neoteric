package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.multiblock.INCMultiblockController;
import net.minecraft.core.BlockPos;

public class FusionReactorController implements INCMultiblockController {
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
