package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Behavior of a formed multiblock. Both ticks may be called independently per side.
 *
 * <p><b>Threading:</b> {@link #tickServer} runs on a per-level executor thread, NOT the main
 * server thread. Any world mutation inside MUST be queued back via
 * {@code ((ServerLevel) level).getServer().execute(...)}.
 */
public interface IMultiblockLogic {
    void onFormed(Level level, BlockPos controllerPos, IMultiblockCache cache);

    void onBroken(Level level, BlockPos controllerPos, IMultiblockCache cache);

    void tickServer(Level level, BlockPos controllerPos, IMultiblockCache cache);

    void tickClient(Level level, BlockPos controllerPos);
}
