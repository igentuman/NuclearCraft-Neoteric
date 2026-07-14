package igentuman.nc.api.multiblock;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Predicate matching a block state and optional block entity, with factories for common cases. */
@FunctionalInterface
public interface BlockPredicate {
    boolean test(BlockState state, @Nullable BlockEntity be);

    static BlockPredicate of(Block block) {
        return (state, be) -> state.is(block);
    }

    static BlockPredicate ofTag(TagKey<Block> tag) {
        return (state, be) -> state.is(tag);
    }

    static BlockPredicate any() {
        return (state, be) -> true;
    }

    static BlockPredicate air() {
        return (state, be) -> state.isAir();
    }
}
