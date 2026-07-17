package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class PumpBE extends NCProcessorBE {

    public PumpBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.PUMP);
        contentHandler().fluidHandler.tanks.get(0).setCapacity(1000000);
    }

    @Override
    public void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().setParallelProcessing(parallelRecipes());
            recipeInfo().ticks = (int) (getBaseProcessTime() * recipe.getTimeModifier());
            recipeInfo().energy = getBasePower() * recipe.getEnergy();
            recipeInfo().radiation = recipeInfo.recipe.getRadiation();
            recipeInfo().be = this;
            recipeInfo().ticksProcessed = 0;
           // recipe.extractInputs(contentHandler);
        }
    }

    //just check if it has solid blocks below and is not busy on other recipes
    public boolean isInSituValid() {
        BlockPosInstance pos = BlockPosInstance.of(getBlockPos());
        for (int i = 0; i < 2; i++) {
            if (!level.getBlockState(pos.below()).isSolidRender(level, pos)) {
                return false;
            }
        }
        return !hasRecipe();
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe != null && getRecipe() != null;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.PUMP;
        }
    }
}
