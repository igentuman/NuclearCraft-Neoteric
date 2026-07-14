package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Defines a formed multiblock's behavior: formation, breaking, and server/client tick callbacks. */
public interface IMultiblockLogic {
    void onFormed(Level level, BlockPos controllerPos, IMultiblockCache cache);

    void onBroken(Level level, BlockPos controllerPos, IMultiblockCache cache);

    void tickServer(Level level, BlockPos controllerPos, IMultiblockCache cache);

    void tickClient(Level level, BlockPos controllerPos);
}
