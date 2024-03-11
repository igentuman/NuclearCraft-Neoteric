package igentuman.nc.multiblock;

import net.minecraft.tileentity.TileEntity;

public interface IMultiblockAttachable {
    void setMultiblock(AbstractNCMultiblock multiblock);

    TileEntity controller();
    AbstractNCMultiblock multiblock();

    boolean canInvalidateCache();
}
