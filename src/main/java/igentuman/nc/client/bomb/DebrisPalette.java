package igentuman.nc.client.bomb;

import igentuman.nc.setup.level.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public final class DebrisPalette {

    private record Rule(List<TagKey<Biome>> tags, BlockState[] blocks) {

        boolean matches(Holder<Biome> biome) {
            for (TagKey<Biome> tag : tags) {
                if (biome.is(tag)) return true;
            }
            return false;
        }
    }

    private static List<Rule> rules;
    private static BlockState[] fallback;

    private DebrisPalette() {}

    public static BlockState pick(Holder<Biome> biome, RandomSource rng) {
        if (rules == null) build();
        BlockState[] blocks = fallback;
        for (Rule rule : rules) {
            if (rule.matches(biome)) {
                blocks = rule.blocks;
                break;
            }
        }
        return blocks[rng.nextInt(blocks.length)];
    }

    private static void build() {
        rules = List.of(
                rule(List.of(ModBiomes.WASTELAND_TAG, Tags.Biomes.IS_WASTELAND),
                        Blocks.COARSE_DIRT, Blocks.COARSE_DIRT, Blocks.GRAVEL, Blocks.COBBLESTONE, Blocks.DIRT),
                rule(List.of(BiomeTags.IS_NETHER),
                        Blocks.NETHERRACK, Blocks.NETHERRACK, Blocks.BLACKSTONE, Blocks.SOUL_SOIL),
                rule(List.of(BiomeTags.IS_END),
                        Blocks.END_STONE, Blocks.END_STONE, Blocks.END_STONE, Blocks.OBSIDIAN),
                rule(List.of(Tags.Biomes.IS_MUSHROOM),
                        Blocks.MYCELIUM, Blocks.DIRT, Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK,
                        Blocks.MUSHROOM_STEM),
                rule(List.of(BiomeTags.IS_BADLANDS),
                        Blocks.RED_SAND, Blocks.RED_SAND, Blocks.TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
                        Blocks.RED_SANDSTONE),
                rule(List.of(Tags.Biomes.IS_DESERT),
                        Blocks.SAND, Blocks.SAND, Blocks.SAND, Blocks.SANDSTONE),
                rule(List.of(Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY),
                        Blocks.SNOW_BLOCK, Blocks.SNOW_BLOCK, Blocks.PACKED_ICE, Blocks.SPRUCE_LOG, Blocks.DIRT),
                rule(List.of(BiomeTags.IS_BEACH, Tags.Biomes.IS_SANDY),
                        Blocks.SAND, Blocks.SAND, Blocks.SANDSTONE, Blocks.GRAVEL),
                rule(List.of(Tags.Biomes.IS_SWAMP),
                        Blocks.MUD, Blocks.MUD, Blocks.DIRT, Blocks.OAK_LOG, Blocks.MOSS_BLOCK),
                rule(List.of(BiomeTags.IS_JUNGLE),
                        Blocks.JUNGLE_LOG, Blocks.JUNGLE_LOG, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.JUNGLE_LEAVES),
                rule(List.of(BiomeTags.IS_TAIGA, Tags.Biomes.IS_CONIFEROUS_TREE),
                        Blocks.SPRUCE_LOG, Blocks.SPRUCE_LOG, Blocks.PODZOL, Blocks.DIRT, Blocks.SPRUCE_LEAVES),
                rule(List.of(BiomeTags.IS_SAVANNA),
                        Blocks.ACACIA_LOG, Blocks.COARSE_DIRT, Blocks.DIRT, Blocks.GRASS_BLOCK),
                rule(List.of(Tags.Biomes.IS_BIRCH_FOREST),
                        Blocks.BIRCH_LOG, Blocks.BIRCH_LOG, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.BIRCH_LEAVES),
                rule(List.of(BiomeTags.IS_FOREST),
                        Blocks.OAK_LOG, Blocks.OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.DIRT, Blocks.GRASS_BLOCK,
                        Blocks.OAK_LEAVES),
                rule(List.of(BiomeTags.IS_MOUNTAIN, Tags.Biomes.IS_STONY_SHORES, Tags.Biomes.IS_WINDSWEPT),
                        Blocks.STONE, Blocks.STONE, Blocks.COBBLESTONE, Blocks.GRAVEL, Blocks.ANDESITE),
                rule(List.of(BiomeTags.IS_OCEAN, BiomeTags.IS_RIVER),
                        Blocks.SAND, Blocks.GRAVEL, Blocks.GRAVEL, Blocks.CLAY),
                rule(List.of(Tags.Biomes.IS_PLAINS),
                        Blocks.DIRT, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT));
        fallback = states(Blocks.DIRT, Blocks.DIRT, Blocks.COBBLESTONE, Blocks.GRAVEL, Blocks.STONE);
    }

    private static Rule rule(List<TagKey<Biome>> tags, Block... blocks) {
        return new Rule(tags, states(blocks));
    }

    private static BlockState[] states(Block... blocks) {
        BlockState[] states = new BlockState[blocks.length];
        for (int i = 0; i < blocks.length; i++) states[i] = blocks[i].defaultBlockState();
        return states;
    }
}
