package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.api.nc.multiblock.MultiblockController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class TurbineController implements MultiblockController {

    protected final TurbineControllerBE controllerBE;
    public TurbineController(TurbineControllerBE turbineControllerBE) {
        controllerBE = turbineControllerBE;
    }

    @Override
    public TurbineControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isInternalValid = false;
        controllerBE().isCasingValid = false;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().orientation = Direction.NORTH;
        controllerBE().coilsEfficiency = 0;
        controllerBE().activeCoils = 0;
        controllerBE().blades = 0;
        controllerBE().flow = 0;
    }
}
