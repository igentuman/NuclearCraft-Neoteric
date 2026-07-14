package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Decides whether a multiblock is correctly assembled around a controller and records its positions. */
public interface IMultiblockValidator {
    boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache);
}
