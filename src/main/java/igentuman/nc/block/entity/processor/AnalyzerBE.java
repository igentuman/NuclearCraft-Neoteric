package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;

import static net.minecraft.item.Items.FILLED_MAP;
import static net.minecraft.item.Items.PAPER;

public class AnalyzerBE extends NCProcessorBE<AnalyzerBE.Recipe> {
    public AnalyzerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ANALYZER);
    }
    public HashMap<Long, OreVeinRecipe> veinsCache = new HashMap<>();
    private BlockPos alreadySearched;

    @Override
    public String getName() {
        return Processors.ANALYZER;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStack[] output,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier,1);
        }

        @Override
        public String getCodeId() {
            return Processors.ANALYZER;
        }
    }

    public void tickServer() {
        if(worldPosition.equals(alreadySearched))
        {
            return;
        }
        super.tickServer();
    }


    protected void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo.isCompleted()) {
            handleChunkAnalyzeWithPaper();
            handleMapAnalyze();
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
            } else {
                recipeInfo.stuck = true;
            }
        }
    }

    private void handleMapAnalyze() {
        if(recipe.getInputIngredient(0).test(new ItemStack(FILLED_MAP))) {
            for (ItemStack output : recipe.getResultItems()) {
                output.setTag(contentHandler.itemHandler.holdedInputs.get(0).getOrCreateTag());
                output.getOrCreateTag().putBoolean("is_nc_analyzed", true);
            }
        }
    }

    private void handleChunkAnalyzeWithPaper() {
        if(recipe.getInputIngredient(0).test(new ItemStack(PAPER))) {
            OreVeinRecipe vein = getVein();
            alreadySearched = worldPosition;
            if (vein == null) {
                for (ItemStack output : recipe.getResultItems()) {
                    output.getOrCreateTag().putString("vein", "nc.ore_vein.none");
                }
            } else {
                for (ItemStack output : recipe.getResultItems()) {
                    output.getOrCreateTag().putString("vein", "nc.ore_vein." + vein.getId().getPath().replace("nc_ore_veins/", ""));
                }
            }
            for (ItemStack output : recipe.getResultItems()) {
                output.getOrCreateTag().putLong("pos", worldPosition.asLong());
                output.getOrCreateTag().putBoolean("is_nc_analyzed", true);
            }
        }
    }

    protected OreVeinRecipe getVein() {
        long pos = new ChunkPos(worldPosition).toLong();
        if(!veinsCache.containsKey(pos))
        {
            veinsCache.put(pos, OreVeinProvider.get((ServerWorld) level)
                    .getVeinForChunk(ChunkPos.getX(pos), ChunkPos.getZ(pos)));
        }
        return veinsCache.get(pos);
    }
}
