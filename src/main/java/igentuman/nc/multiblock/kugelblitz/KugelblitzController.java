package igentuman.nc.multiblock.kugelblitz;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.entity.kugelblitz.ChamberTerminalBE;
import net.minecraft.core.BlockPos;

public class KugelblitzController  implements MultiblockController {

    private final ChamberTerminalBE controllerBE;

    public KugelblitzController(ChamberTerminalBE be) {
        controllerBE = be;
    }

    @Override
    public ChamberTerminalBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().isCasingValid = false;
        controllerBE().isInternalValid = false;
    }

    @Override
    public void setErroredBlock(BlockPos relative) {
        controllerBE().errorBlockPos = relative;
    }
}
