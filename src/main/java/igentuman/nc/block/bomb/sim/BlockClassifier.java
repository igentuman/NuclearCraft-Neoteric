package igentuman.nc.block.bomb.sim;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.NuclearBlastRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
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

    public static class Transformation {
        public final BlockState outputState;
        public final double chance;

        public Transformation(BlockState outputState, double chance) {
            this.outputState = outputState;
            this.chance = chance;
        }
    }

    private static volatile Map<Block, List<Transformation>> transformationMap = new HashMap<>();
    private static boolean recipesLoaded = false;

    public static synchronized void initializeRecipesMap() {
        if (recipesLoaded) return;
        try {
            var recipeTypeObj = NcRecipeType.ALL_RECIPES.get("nuclear_blast");
            if (recipeTypeObj == null) return;
            var recipeType = recipeTypeObj.getRecipeType();
            if (recipeType == null) return;
            List<NuclearBlastRecipe> recipes = (List<NuclearBlastRecipe>) recipeType.getRecipes(null);
            if (recipes == null || recipes.isEmpty()) {
                return;
            }

            Map<Block, List<Transformation>> tempMap = new HashMap<>();

            for (NuclearBlastRecipe recipe : recipes) {
                if (recipe.isIncomplete()) continue;

                if (recipe.getInputItems().length == 0 || recipe.getInputItems()[0] == null) continue;
                if (recipe.getOutputItems().length == 0 || recipe.getOutputItems()[0] == null) continue;

                ItemStackIngredient inputIngredient = recipe.getInputItems()[0];
                ItemStackIngredient outputIngredient = recipe.getOutputItems()[0];

                List<ItemStack> outputStacks = outputIngredient.getRepresentations();
                if (outputStacks.isEmpty()) continue;
                ItemStack outputStack = outputStacks.get(0);
                if (outputStack.isEmpty()) continue;

                Block outputBlock = Block.byItem(outputStack.getItem());
                if (outputBlock == Blocks.AIR) continue;

                BlockState outputState = outputBlock.defaultBlockState();
                double chance = recipe.getChance();
                Transformation transformation = new Transformation(outputState, chance);

                List<ItemStack> inputStacks = inputIngredient.getRepresentations();
                for (ItemStack inputStack : inputStacks) {
                    if (inputStack.isEmpty()) continue;
                    Block inputBlock = Block.byItem(inputStack.getItem());
                    if (inputBlock == Blocks.AIR) continue;

                    tempMap.computeIfAbsent(inputBlock, k -> new ArrayList<>()).add(transformation);
                }
            }

            transformationMap = tempMap;
            recipesLoaded = true;
        } catch (Exception e) {
            // Quietly ignore or log
        }
    }

    public static synchronized void invalidate() {
        recipesLoaded = false;
        transformationMap.clear();
    }

    // ratioSq = (distance / blastRadius)^2. Thresholds below are zone-radius fractions, squared.
    // Vaporization zone: inner 15% of blast radius (0.15^2). Everything becomes air.
    private static final double VAPORIZATION_ZONE_SQ = 0.0225;
    // Ash zone: inner 25% (0.25^2). Air, with rare residual fire on stone near ground.
    private static final double ASH_ZONE_SQ = 0.0625;
    // Inner edge of ash zone where residual fire may spawn (0.22^2).
    private static final double ASH_ZONE_FIRE_EDGE_SQ = 0.0484;
    // Severe damage zone: inner 40% (0.40^2). Wood charred, dirt/sand churned, stone fractured.
    private static final double SEVERE_DAMAGE_ZONE_SQ = 0.16;
    // Moderate damage zone: inner 70% (0.70^2). Surface scouring, sparse block loss.
    private static final double MODERATE_DAMAGE_ZONE_SQ = 0.49;

    private BlockClassifier() {}

    private static boolean isGrassPlant(BlockState state) {
        return state.getBlock() == Blocks.GRASS
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

            return new BlastOp.SetBlock(pos, NCBlocks.WASTELAND_EARTH.get().defaultBlockState());
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
            initializeRecipesMap();
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
