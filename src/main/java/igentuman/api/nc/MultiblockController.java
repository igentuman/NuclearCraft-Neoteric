package igentuman.api.nc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface MultiblockController {

    BlockEntity controllerBE();

    void clearStats();

    void addErroredBlock(BlockPos relative);
}
