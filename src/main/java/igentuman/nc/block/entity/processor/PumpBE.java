package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

public class PumpBE extends NCProcessorBE<PumpBE.Recipe> {
    public PumpBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.PUMP);
    }
    @Override
    public String getName() {
        return Processors.PUMP;
    }

    @Override
    protected void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = (int) (getBaseProcessTime() * recipe.getTimeModifier());
            recipeInfo.energy = getBasePower() * recipe.getEnergy();
            recipeInfo.radiation = recipeInfo.recipe.getRadiation();
            recipeInfo.be = this;
           // recipe.extractInputs(contentHandler);
        }
    }

    //just check if it has solid blocks below and is not busy on other recipes
    public boolean isInSituValid() {
        NCBlockPos pos = NCBlockPos.of(getBlockPos());
        for (int i = 0; i < 4; i++) {
            if (!level.getBlockState(pos.below()).isSolidRender(level, pos)) {
                return false;
            }
        }
        return !hasRecipe();
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStack[] output,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.PUMP;
        }
    }
}
