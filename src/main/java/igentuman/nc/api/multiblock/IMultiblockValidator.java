package igentuman.nc.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Validator decides whether a multiblock structure is correctly assembled around the controller.
 * Implementations are fully self-contained; pattern usage is optional.
 *
 * <p>Implementations MUST populate {@code cache.getStructurePositions()} with every block position
 * (including the controller) that belongs to the structure. The handler uses this set to index
 * positions for O(1) lookup on block-change events.
 */
public interface IMultiblockValidator {
    boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache);
}
