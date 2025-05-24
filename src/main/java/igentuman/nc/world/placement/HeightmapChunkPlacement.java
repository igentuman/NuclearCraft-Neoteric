package igentuman.nc.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;
import static igentuman.nc.world.placement.NCPlacementModifierTypes.HEIGHTMAP_CHUNK;
import static net.minecraft.tags.BlockTags.*;
import static net.minecraft.world.level.block.Blocks.WATER;

public class HeightmapChunkPlacement extends PlacementModifier {
    public static final Codec<HeightmapChunkPlacement> CODEC = RecordCodecBuilder.create((p_191701_) -> p_191701_.group(Types.CODEC.fieldOf("heightmap").forGetter((p_191705_) -> p_191705_.heightmap)).apply(p_191701_, HeightmapChunkPlacement::new));
    private final Heightmap.Types heightmap;

    private HeightmapChunkPlacement(Heightmap.Types p_191699_) {
        this.heightmap = p_191699_;
    }

    public static HeightmapChunkPlacement onHeightmap(Heightmap.Types pHeightmap) {
        return new HeightmapChunkPlacement(pHeightmap);
    }

    public Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
        int chunkOriginX = pPos.getX() & ~15;
        int chunkOriginZ = pPos.getZ() & ~15;

        final List<BlockPos> positions = new ArrayList<>();

        for (int x = chunkOriginX; x < chunkOriginX + 16; x++) {
            for (int z = chunkOriginZ; z < chunkOriginZ + 16; z++) {
                int height = pContext.getHeight(this.heightmap, x, z)-1;

                if (height > pContext.getMinBuildHeight()) {
                    final BlockPos pos = new BlockPos(x, height, z);
                    final BlockState testBs = pContext.getBlockState(pos);
                    if(!pContext.getLevel().getBiome(pos).is(WASTELAND) ||  testBs.is(WATER)) continue;
                    if(!(testBs.is(SAND) || testBs.is(DIRT) || testBs.is(TERRACOTTA))) {
                        height--;
                    }
                    positions.add(new BlockPos(x, height, z));
                }
            }
        }

        return positions.stream();
    }

    public PlacementModifierType<?> type() {
        return HEIGHTMAP_CHUNK.get();
    }
}

