package igentuman.nc.recipes.lookup;

import igentuman.nc.util.sided.capability.NCItemStackHandler;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.cache.InputRecipeCache;
import igentuman.nc.recipes.cache.SingleInputRecipeCache;
import igentuman.nc.recipes.inputs.IInputHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Helper expansion of {@link IRecipeLookupHandler} for easily implementing contains and find recipe lookups for recipes that take a single input using the input cache.
 */
public interface ISingleRecipeLookupHandler<INPUT, RECIPE extends NcRecipe & Predicate<INPUT>, INPUT_CACHE extends SingleInputRecipeCache<INPUT, ?, RECIPE, ?>>
      extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipe(INPUT input) {
        return getRecipeType().getInputCache().containsInput(getHandlerWorld(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given input against the recipe type's input cache.
     *
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(INPUT input) {
        return getRecipeType().getInputCache().findFirstRecipe(getHandlerWorld(), input);
    }

    @Nullable
    default RECIPE findFirstRecipe(NCItemStackHandler input) {
        return getRecipeType().getInputCache().findFirstRecipe(getHandlerWorld(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given input against the recipe type's input cache.
     *
     * @param inputHandler Input handler to grab the recipe input from.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<INPUT> inputHandler) {
        return findFirstRecipe(inputHandler.getInput());
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface ItemRecipeLookupHandler<RECIPE extends NcRecipe & Predicate<ItemStack>> extends ISingleRecipeLookupHandler<ItemStack, RECIPE, InputRecipeCache.SingleItem<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link ISingleRecipeLookupHandler} not as messy.
     */
    interface FluidRecipeLookupHandler<RECIPE extends NcRecipe & Predicate<FluidStack>> extends ISingleRecipeLookupHandler<FluidStack, RECIPE, InputRecipeCache.SingleFluid<RECIPE>> {
    }
}