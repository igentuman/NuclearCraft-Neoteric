package igentuman.nc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import static net.minecraft.world.level.block.Blocks.AIR;

public class WorldUtil {

    public static BlockState getBlockState(BlockPos pos, ServerLevel level) {
        if (level == null) {
            return AIR.defaultBlockState();
        }

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkAccess chunk = getChunk(chunkX, chunkZ, level);

        if (chunk == null) {
            return AIR.defaultBlockState();
        }

        int sectionIndex = level.getSectionIndex(pos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return AIR.defaultBlockState();
        }

        LevelChunkSection section = chunk.getSections()[sectionIndex];
        if (section == null || section.hasOnlyAir()) {
            return AIR.defaultBlockState();
        }

        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return section.getBlockState(localX, localY, localZ);
    }

    public static BlockEntity getBlockEntity(BlockPos pos, ServerLevel level) {
        if (level == null) {
            return null;
        }

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkAccess chunk = getChunk(chunkX, chunkZ, level);

        if (chunk == null) {
            return null;
        }

        return chunk.getBlockEntity(pos);
    }

    public static ChunkAccess getChunk(int chunkX, int chunkZ, ServerLevel level) {
        return level.getChunkSource().getChunk(chunkX, chunkZ, true);
    }
}
