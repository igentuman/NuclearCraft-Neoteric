package igentuman.nc.recipes.lookup;

import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.cache.DoubleInputRecipeCache;
import igentuman.nc.recipes.cache.InputRecipeCache;
import igentuman.nc.recipes.inputs.IInputHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

/**
 * Helper expansion of {@link IRecipeLookupHandler} for easily implementing contains and find recipe lookups for recipes that takes two inputs.
 */
public interface IDoubleRecipeLookupHandler<INPUT_A, INPUT_B, RECIPE extends NcRecipe & BiPredicate<INPUT_A, INPUT_B>,
      INPUT_CACHE extends DoubleInputRecipeCache<INPUT_A, ?, INPUT_B, ?, RECIPE, ?, ?>> extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link DoubleInputRecipeCache#containsInputAB(Level, Object, Object)} and {@link DoubleInputRecipeCache#containsInputBA(Level, Object, Object)} for
     * more details about when this method should be called versus when {@link #containsRecipeBA(Object, Object)} should be called.
     */
    default boolean containsRecipeAB(INPUT_A inputA, INPUT_B inputB) {
        return getRecipeType().getInputCache().containsInputAB(getHandlerWorld(), inputA, inputB);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link DoubleInputRecipeCache#containsInputAB(Level, Object, Object)} and {@link DoubleInputRecipeCache#containsInputBA(Level, Object, Object)} for
     * more details about when this method should be called versus when {@link #containsRecipeAB(Object, Object)} should be called.
     */
    default boolean containsRecipeBA(INPUT_A inputA, INPUT_B inputB) {
        return getRecipeType().getInputCache().containsInputBA(getHandlerWorld(), inputA, inputB);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipeA(INPUT_A input) {
        return getRecipeType().getInputCache().containsInputA(getHandlerWorld(), input);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipeB(INPUT_B input) {
        return getRecipeType().getInputCache().containsInputB(getHandlerWorld(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given inputs against the recipe type's input cache.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(INPUT_A inputA, INPUT_B inputB) {
        return getRecipeType().getInputCache().findFirstRecipe(getHandlerWorld(), inputA, inputB);
    }

    @Nullable
    default RECIPE findFirstRecipe(ItemCapabilityHandler itemHandler) {
        if(getRecipeType() == null) return null;
        return findFirstRecipe((INPUT_A) itemHandler.getStackInSlot(0), (INPUT_B) itemHandler.getStackInSlot(1));
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given inputs against the recipe type's input cache.
     *
     * @param inputAHandler Input handler to grab the first recipe input from.
     * @param inputBHandler Input handler to grab the second recipe input from.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<INPUT_A> inputAHandler, IInputHandler<INPUT_B> inputBHandler) {
        return findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput());
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link IDoubleRecipeLookupHandler} not as messy.
     */
    interface DoubleItemRecipeLookupHandler<RECIPE extends NcRecipe & BiPredicate<ItemStack, ItemStack>> extends
          IDoubleRecipeLookupHandler<ItemStack, ItemStack, RECIPE, InputRecipeCache.DoubleItem<RECIPE>> {
    }

}