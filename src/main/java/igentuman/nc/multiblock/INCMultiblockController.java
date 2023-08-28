package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface INCMultiblockController {
    BlockEntity controllerBE();

    void clearStats();

    void addErroredBlock(BlockPos relative);
}
