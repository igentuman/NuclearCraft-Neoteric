package igentuman.nc.recipes.lookup;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.cache.FourInputRecipeCache;
import igentuman.nc.recipes.cache.InputRecipeCache;
import igentuman.nc.recipes.inputs.IInputHandler;
import igentuman.nc.util.functions.FourPredicate;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IFourRecipeLookupHandler<INPUT_A, INPUT_B, INPUT_C, INPUT_D, RECIPE extends NcRecipe & FourPredicate<INPUT_A, INPUT_B, INPUT_C, INPUT_D>,
      INPUT_CACHE extends FourInputRecipeCache<INPUT_A, ?, INPUT_B, ?, INPUT_C, ?, INPUT_D, ?, RECIPE, ?, ?, ?, ?>> extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {


    default boolean containsRecipeA(INPUT_A input) {
        return getRecipeType().getInputCache().containsInputA(getHandlerWorld(), input);
    }

    default boolean containsRecipeB(INPUT_B input) {
        return getRecipeType().getInputCache().containsInputB(getHandlerWorld(), input);
    }

    default boolean containsRecipeC(INPUT_C input) {
        return getRecipeType().getInputCache().containsInputC(getHandlerWorld(), input);
    }

    default boolean containsRecipeD(INPUT_D input) {
        return getRecipeType().getInputCache().containsInputD(getHandlerWorld(), input);
    }

    @Nullable
    default RECIPE findFirstRecipe(INPUT_A inputA, INPUT_B inputB, INPUT_C inputC, INPUT_D inputD) {
        return getRecipeType().getInputCache().findFirstRecipe(getHandlerWorld(), inputA, inputB, inputC, inputD);
    }

    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<INPUT_A> inputAHandler, IInputHandler<INPUT_B> inputBHandler, IInputHandler<INPUT_C> inputCHandler, IInputHandler<INPUT_D> inputDHandler) {
        return findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput(), inputCHandler.getInput(), inputDHandler.getInput());
    }

    interface FourItemRecipeLookupHandler<RECIPE extends NcRecipe & FourPredicate<ItemStack, ItemStack, ItemStack, ItemStack>> extends
            IFourRecipeLookupHandler<ItemStack, ItemStack, ItemStack, ItemStack,RECIPE, InputRecipeCache.FourItem<RECIPE>> {
    }

}