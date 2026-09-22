package igentuman.nc.multiblock.validation;

import igentuman.nc.multiblock.MultiblockLevelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ServerLevelStructureReader implements LoadedStructureReader {

    private final ServerLevel level;
    private final MultiblockLevelState state;

    public ServerLevelStructureReader(ServerLevel level, MultiblockLevelState state) {
        this.level = level;
        this.state = state;
    }

    @Override
    public boolean isLoaded(SectionPos sectionPos) {
        return level.hasChunk(sectionPos.x(), sectionPos.z());
    }

    @Override
    public BlockState blockState(BlockPos pos) {
        requireLoaded(pos);
        return level.getBlockState(pos);
    }

    @Override
    @Nullable
    public BlockEntity blockEntity(BlockPos pos) {
        requireLoaded(pos);
        return level.getBlockEntity(pos);
    }

    @Override
    public long sectionRevision(SectionPos sectionPos) {
        return state.sectionRevision(sectionPos.asLong());
    }

    @Override
    public void validationObserved(java.util.UUID structureId, BlockPos pos, BlockState observed) {
        state.validationObserved(structureId, pos, observed);
    }

    @Override
    public long structuralRevision(java.util.UUID structureId) {
        return state.structure(structureId).map(record -> record.topologyRevision()).orElse(-1L);
    }

    @Override
    public long configRevision() { return state.configRevision(); }

    private void requireLoaded(BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            throw new IllegalStateException("Attempted to read an unloaded structure position " + pos);
        }
    }
}
