package igentuman.nc.multiblock;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMultiblockAttachable {
    void setMultiblock(AbstractNCMultiblock multiblock);

    BlockEntity controller();
    AbstractNCMultiblock multiblock();

    boolean canInvalidateCache();
}
