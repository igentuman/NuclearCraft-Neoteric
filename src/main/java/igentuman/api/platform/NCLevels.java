package igentuman.api.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Platform translation layer for Level utilities.
 * <p>
 * Forge added {@code getExistingBlockEntity} as an optimised lookup that
 * only returned already-loaded block entities. NeoForge 1.21.1 removed it;
 * the vanilla {@code getBlockEntity} now has the same behaviour (returns
 * the cached BE or null, never forces a load).
 */
public final class NCLevels {

    private NCLevels() {}

    /**
     * Replaces {@code level.getExistingBlockEntity(pos)}.
     */
    @Nullable
    public static BlockEntity getExistingBlockEntity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos);
    }
}
