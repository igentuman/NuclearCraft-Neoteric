package igentuman.nc.multiblock.heat_exchanger;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import net.minecraft.core.BlockPos;

public class HeatExchangerController implements MultiblockController {

    protected HeatExchangerControllerBE controllerBE;

    public HeatExchangerController(HeatExchangerControllerBE heatExchangerControllerBE) {
        controllerBE = heatExchangerControllerBE;
    }

    @Override
    public HeatExchangerControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().heatExchangers = 0;
    }

    @Override
    public void setControllerBe(MultiblockControllerBE multiblockControllerBE) {
        controllerBE = (HeatExchangerControllerBE) multiblockControllerBE;
    }
}
