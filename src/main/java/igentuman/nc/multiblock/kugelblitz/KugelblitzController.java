package igentuman.nc.multiblock.kugelblitz;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.kugelblitz.ChamberTerminalBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class KugelblitzController  implements MultiblockController {
    private final ChamberTerminalBE controllerBE;
    public KugelblitzController(ChamberTerminalBE be) {
        controllerBE = be;
    }

    @Override
    public BlockEntity controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {

    }

    @Override
    public void addErroredBlock(BlockPos relative) {

    }
}
