package igentuman.nc.entity.bomb.sim;

import igentuman.nc.recipe.bomb.NcBlastRecipes;
import igentuman.nc.recipe.bomb.NuclearBlastRecipe;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.world.level.block.Blocks.*;

public final class BlockClassifier {

    private static final double VAPORIZATION_ZONE_SQ = 0.0225;
    private static final double ASH_ZONE_SQ = 0.0625;
    private static final double ASH_ZONE_FIRE_EDGE_SQ = 0.0484;
    private static final double SEVERE_DAMAGE_ZONE_SQ = 0.16;
    private static final double MODERATE_DAMAGE_ZONE_SQ = 0.49;

    private static volatile BlockState wastelandEarth;

    public static final class Transformation {
        public final BlockState outputState;
        public final double chance;

        public Transformation(BlockState outputState, double chance) {
            this.outputState = outputState;
            this.chance = chance;
        }
    }

    private static volatile Map<Block, List<Transformation>> transformationMap = Map.of();

    public static void initializeRecipesMap(ServerLevel server) {
        Map<Block, List<Transformation>> tempMap = new HashMap<>();
        for (RecipeHolder<NuclearBlastRecipe> holder : server.getRecipeManager().getAllRecipesFor(NcBlastRecipes.TYPE.get())) {
            NuclearBlastRecipe recipe = holder.value();
            ItemStack outputStack = recipe.output().resolve();
            if (outputStack.isEmpty()) continue;
            Block outputBlock = Block.byItem(outputStack.getItem());
            if (outputBlock == Blocks.AIR) continue;
            Transformation transformation = new Transformation(outputBlock.defaultBlockState(), recipe.chance());
            for (ItemStack inputStack : recipe.input().getItems()) {
                if (inputStack.isEmpty()) continue;
                Block inputBlock = Block.byItem(inputStack.getItem());
                if (inputBlock == Blocks.AIR) continue;
                tempMap.computeIfAbsent(inputBlock, k -> new ArrayList<>()).add(transformation);
            }
        }
        transformationMap = tempMap;
    }

    private BlockClassifier() {}

    private static BlockState wastelandEarth() {
        BlockState state = wastelandEarth;
        if (state == null) {
            state = ModEntries.get("wasteland_earth").block().get().defaultBlockState();
            wastelandEarth = state;
        }
        return state;
    }

    private static boolean isGrassPlant(BlockState state) {
        return state.getBlock() == Blocks.SHORT_GRASS
                || state.getBlock() == Blocks.TALL_GRASS
                || state.getBlock() == Blocks.FERN
                || state.getBlock() == Blocks.LARGE_FERN
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS);
    }

    private static boolean isStoneLike(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.getBlock() == Blocks.COBBLESTONE
                || state.getBlock() == Blocks.COBBLED_DEEPSLATE
                || state.getBlock() == Blocks.STONE_BRICKS
                || state.getBlock() == Blocks.DEEPSLATE_BRICKS
                || state.getBlock() == Blocks.MOSSY_COBBLESTONE
                || state.getBlock() == Blocks.MOSSY_STONE_BRICKS;
    }

    private static BlastOp getDirtOrSandBlastOp(BlockPos pos, BlockState state) {
        float r = ThreadLocalRandom.current().nextFloat();
        if (r < 0.80f) {
            return new BlastOp.SetBlock(pos, wastelandEarth());
        } else if (r < 0.85f) {
            if (state.is(BlockTags.SAND)) {
                return new BlastOp.SetBlock(pos, GLASS.defaultBlockState());
            }
            return new BlastOp.SetBlock(pos, Blocks.ROOTED_DIRT.defaultBlockState());
        } else if (r < 0.90f) {
            if (state.is(BlockTags.SAND)) {
                return new BlastOp.SetBlock(pos, GLASS.defaultBlockState());
            }
            return new BlastOp.SetBlock(pos, Blocks.COARSE_DIRT.defaultBlockState());
        } else if (r < 0.97f) {
            if (state.is(BlockTags.SAND)) {
                return new BlastOp.SetBlock(pos, GLASS.defaultBlockState());
            }
            return new BlastOp.SetBlock(pos, Blocks.GRAVEL.defaultBlockState());
        }
        return null;
    }

    public static BlastOp classify(BlockPos pos, BlockState state, double ratioSq, int relY) {
        if (state.isAir()) return null;

        if (ratioSq < VAPORIZATION_ZONE_SQ) {
            return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
        }
        if (ratioSq < ASH_ZONE_SQ) {
            if (ratioSq >= ASH_ZONE_FIRE_EDGE_SQ && relY <= 0 && isStoneLike(state)
                    && ThreadLocalRandom.current().nextFloat() < 0.0006f) {
                return new BlastOp.SetBlock(pos, Blocks.FIRE.defaultBlockState());
            }
            return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
        }
        if (ratioSq < SEVERE_DAMAGE_ZONE_SQ) {
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.WOODEN_STAIRS)) {
                if (ThreadLocalRandom.current().nextFloat() < 0.15f) {
                    return new BlastOp.SetBlock(pos, Blocks.FIRE.defaultBlockState());
                }
                return new BlastOp.SetBlock(pos, Blocks.COAL_BLOCK.defaultBlockState());
            }
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.SWORD_EFFICIENT) || state.is(BlockTags.SNOW)) {
                return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
            }
            if (state.is(BlockTags.WOOL) || state.is(BlockTags.WOOL_CARPETS)) {
                return new BlastOp.SetBlock(pos, Blocks.FIRE.defaultBlockState());
            }
            if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)) {
                return getDirtOrSandBlastOp(pos, state);
            }
            List<Transformation> transformations = transformationMap.get(state.getBlock());
            if (transformations != null) {
                for (Transformation t : transformations) {
                    if (ThreadLocalRandom.current().nextDouble() < t.chance) {
                        return new BlastOp.SetBlock(pos, t.outputState);
                    }
                }
                return null;
            }
            if (isGrassPlant(state)) {
                return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
            }
            if (isStoneLike(state)) {
                float r = ThreadLocalRandom.current().nextFloat();
                if (r < 0.5f) {
                    return new BlastOp.SetBlock(pos, Blocks.DEEPSLATE.defaultBlockState());
                } else if (r < 0.8f) {
                    return new BlastOp.SetBlock(pos, Blocks.BASALT.defaultBlockState());
                } else if (r < 0.95f) {
                    return new BlastOp.SetBlock(pos, Blocks.OBSIDIAN.defaultBlockState());
                }
                return new BlastOp.SetBlock(pos, Blocks.FIRE.defaultBlockState());
            }
            return null;
        }
        if (ratioSq < MODERATE_DAMAGE_ZONE_SQ) {
            if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)) {
                return getDirtOrSandBlastOp(pos, state);
            }
            if (isGrassPlant(state)) {
                if (ThreadLocalRandom.current().nextFloat() < 0.25f) {
                    return new BlastOp.SetBlock(pos, Blocks.DEAD_BUSH.defaultBlockState());
                }
                if (ThreadLocalRandom.current().nextFloat() < 0.25f) {
                    return new BlastOp.SetBlock(pos, Blocks.FIRE.defaultBlockState());
                }
                return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
            }
            if (state.is(BlockTags.LEAVES) || state.is(VINE) || state.is(BlockTags.SNOW)) {
                return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
            }
            if (ThreadLocalRandom.current().nextFloat() < 0.06f) {
                return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
            }
            return null;
        }
        if (state.is(BlockTags.LEAVES) || state.is(VINE) || state.is(BlockTags.SNOW) || state.is(BAMBOO_BLOCK)) {
            return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
        }
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.WOOL)) {
            if (ThreadLocalRandom.current().nextFloat() < 0.5f) {
                return new BlastOp.SetBlock(pos, Blocks.FIRE.defaultBlockState());
            }
        }
        if (ThreadLocalRandom.current().nextFloat() < 0.015f) {
            return new BlastOp.SetBlock(pos, Blocks.AIR.defaultBlockState());
        }
        return null;
    }
}
