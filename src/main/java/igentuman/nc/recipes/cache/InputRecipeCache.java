package igentuman.nc.recipes.cache;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.type.FluidInputCache;
import igentuman.nc.recipes.cache.type.ItemInputCache;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.functions.FourPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class InputRecipeCache {

    public static class SingleItem<RECIPE extends NcRecipe & Predicate<ItemStack>>
          extends SingleInputRecipeCache<ItemStack, ItemStackIngredient, RECIPE, ItemInputCache<RECIPE>> {

        public SingleItem(NcRecipeType<RECIPE, ?> recipeType, Function<RECIPE, ItemStackIngredient> inputExtractor) {
            super(recipeType, inputExtractor, new ItemInputCache<>());
        }

    }

    public static class SingleFluid<RECIPE extends NcRecipe & Predicate<FluidStack>>
          extends SingleInputRecipeCache<FluidStack, FluidStackIngredient, RECIPE, FluidInputCache<RECIPE>> {

        public SingleFluid(NcRecipeType<RECIPE, ?> recipeType, Function<RECIPE, FluidStackIngredient> inputExtractor) {
            super(recipeType, inputExtractor, new FluidInputCache<>());
        }
    }


    public static class DoubleItem<RECIPE extends NcRecipe & BiPredicate<ItemStack, ItemStack>>
          extends DoubleInputRecipeCache.DoubleSameInputRecipeCache<ItemStack, ItemStackIngredient, RECIPE, ItemInputCache<RECIPE>> {

        public DoubleItem(NcRecipeType<RECIPE, ?> recipeType, Function<RECIPE, ItemStackIngredient> inputAExtractor,
                          Function<RECIPE, ItemStackIngredient> inputBExtractor) {
            super(recipeType, inputAExtractor, inputBExtractor, ItemInputCache::new);
        }
    }



    public static class FourItem<RECIPE extends NcRecipe & FourPredicate<ItemStack, ItemStack, ItemStack, ItemStack>>
            extends FourInputRecipeCache<
            ItemStack, ItemStackIngredient,
            ItemStack, ItemStackIngredient,
            ItemStack, ItemStackIngredient,
            ItemStack, ItemStackIngredient,
            RECIPE,
            ItemInputCache<RECIPE>, ItemInputCache<RECIPE>, ItemInputCache<RECIPE>, ItemInputCache<RECIPE>> {

        public FourItem(NcRecipeType<RECIPE, ?> recipeType,
                        Function<RECIPE, ItemStackIngredient> inputAExtractor,
                        Function<RECIPE, ItemStackIngredient> inputBExtractor,
                        Function<RECIPE, ItemStackIngredient> inputCExtractor,
                        Function<RECIPE, ItemStackIngredient> inputDExtractor) {
            super(recipeType, inputAExtractor, new ItemInputCache<>(),inputBExtractor, new ItemInputCache<>(), inputCExtractor, new ItemInputCache<>(), inputDExtractor, new ItemInputCache<>());
        }
    }

}