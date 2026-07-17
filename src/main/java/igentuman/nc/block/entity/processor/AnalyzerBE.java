package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.item.CrystalAnalysis;
import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.util.insitu_leaching.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;

import static net.minecraft.world.item.Items.FILLED_MAP;
import static net.minecraft.world.item.Items.PAPER;

public class AnalyzerBE extends NCProcessorBE {

    public HashMap<Long, OreVeinRecipe> veinsCache = new HashMap<>();
    private BlockPos alreadySearched;

    public AnalyzerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ANALYZER);
        particle1 = ParticleTypes.WAX_OFF;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
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


    public void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo.isCompleted()) {
            handleChunkAnalyzeWithPaper();
            handleMapAnalyze();
            if(contentHandler().itemHandler.getStackInSlot(1).isEmpty()) {
                handleCrystalAnalyze();
            }
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
            } else {
                recipeInfo.stuck = true;
            }
        }
    }

    // Rolls rarity + buff onto the output crystal. Re-analysing an already-analyzed crystal preserves
    // its NBT instead of rerolling, so feeding one back in never destroys an existing artifact.
    private void handleCrystalAnalyze() {
        if (recipe.getInputIngredient(0).test(new ItemStack(NCItems.RESONITE_CRYSTAL.get()))) {
            ItemStack input = contentHandler.itemHandler.holdedInputs.get(0);
            for (ItemStack output : recipe.getResultItems()) {
                if (ResoniteCrystalItem.isAnalyzed(input)) {
                    output.setTag(input.getOrCreateTag().copy());
                } else {
                    CrystalAnalysis.applyAnalysis(output, level.random);
                }
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
        long pos = ChunkPos.asLong(worldPosition);
        if(!veinsCache.containsKey(pos))
        {
            veinsCache.put(pos, OreVeinProvider.get((ServerLevel) level)
                    .getVeinForChunk(ChunkPos.getX(pos), ChunkPos.getZ(pos)));
        }
        return veinsCache.get(pos);
    }
}
