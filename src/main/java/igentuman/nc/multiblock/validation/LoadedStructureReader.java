package igentuman.nc.multiblock.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface LoadedStructureReader {

    boolean isLoaded(SectionPos sectionPos);

    BlockState blockState(BlockPos pos);

    default boolean matchesBlock(BlockPos pos, Predicate<BlockState> predicate) {
        return predicate.test(blockState(pos));
    }

    @Nullable
    BlockEntity blockEntity(BlockPos pos);

    long sectionRevision(SectionPos sectionPos);

    default void validationObserved(java.util.UUID structureId, BlockPos pos, BlockState state) {}

    default long structuralRevision(java.util.UUID structureId) { return 0; }

    default long configRevision() { return 0; }
}
