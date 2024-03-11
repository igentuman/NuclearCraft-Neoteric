package igentuman.nc.multiblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public interface INCMultiblockController {
    TileEntity controllerBE();

    void clearStats();

    void addErroredBlock(BlockPos relative);
}
