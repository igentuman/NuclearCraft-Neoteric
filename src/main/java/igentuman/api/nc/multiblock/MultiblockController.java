package igentuman.api.nc.multiblock;

import igentuman.nc.block.entity.MultiblockControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface MultiblockController {

    MultiblockControllerBE controllerBE();

    void clearStats();

    default void setErroredBlock(BlockPos relative) {
        controllerBE().errorBlockPos = relative;
    };
}
