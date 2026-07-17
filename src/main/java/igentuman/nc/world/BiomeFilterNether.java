package igentuman.nc.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static igentuman.nc.setup.registration.WorldGeneration.VEGETATION_MODIFIER;

public class BiomeFilterNether extends PlacementModifier {

    private static final BiomeFilterNether INSTANCE = new BiomeFilterNether();
    public static Codec<BiomeFilterNether> CODEC = Codec.unit(() -> {
        return INSTANCE;
    });

    private BiomeFilterNether() {
    }

    public static BiomeFilterNether biome() {
        return INSTANCE;
    }

    @Override
    public @NotNull Stream<BlockPos> getPositions(PlacementContext context, RandomSource randomSource, BlockPos pos) {
        LevelReader level = context.getLevel();
        Holder<Biome> biome = level.getBiome(pos);
        if(biome.is(Biomes.NETHER_WASTES) || biome.is(Biomes.BASALT_DELTAS) || biome.is(Biomes.WARPED_FOREST)
                || biome.is(Biomes.CRIMSON_FOREST) || biome.is(Biomes.SOUL_SAND_VALLEY)) {
            return Stream.of(pos);
        }
        return Stream.empty();
    }

    public @NotNull PlacementModifierType<?> type() {
        return VEGETATION_MODIFIER.get();
    }
}
