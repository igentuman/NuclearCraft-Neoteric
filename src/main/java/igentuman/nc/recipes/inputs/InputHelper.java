package igentuman.nc.recipes.inputs;

import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.recipes.fluid.IExtendedFluidTank;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.inventory.Action;
import igentuman.nc.util.inventory.IInventorySlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NothingNullByDefault
public class InputHelper {

    private InputHelper() {
    }

    /**
     * Wrap an inventory slot into an {@link IInputHandler}.
     *
     * @param slot           Slot to wrap.
     * @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
     */
    public static IInputHandler<@NotNull ItemStack> getInputHandler(IInventorySlot slot, CachedRecipe.OperationTracker.RecipeError notEnoughError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
        return new IInputHandler<>() {

            @Override
            public ItemStack getInput() {
                return slot.getStack();
            }

            @Override
            public ItemStack getRecipeInput(InputIngredient<@NotNull ItemStack> recipeIngredient) {
                ItemStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return ItemStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(ItemStack recipeInput, int operations) {
                if (operations == 0) {
                    //Just exit if we are somehow here at zero operations
                    return;
                }
                if (!recipeInput.isEmpty()) {
                    int amount = recipeInput.getCount() * operations;
                    logMismatchedStackSize(slot.shrinkStack(amount, Action.EXECUTE), amount);
                }
            }

            @Override
            public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, ItemStack recipeInput, int usageMultiplier) {
                //Only calculate if we need to use anything
                if (usageMultiplier > 0) {
                    //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
                    // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
                    // where we may want to allow not having the input be required for recipe matching
                    if (!recipeInput.isEmpty()) {
                        //TODO: Simulate?
                        int operations = getInput().getCount() / (recipeInput.getCount() * usageMultiplier);
                        if (operations > 0) {
                            tracker.updateOperations(operations);
                            return;
                        }
                    }
                    // Not enough input to match the recipe, reset the progress
                    tracker.resetProgress(notEnoughError);
                }
            }
        };
    }



    /**
     * Wrap a fluid tank into an {@link IInputHandler}.
     *
     * @param tank           Tank to wrap.
     * @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
     */
    public static IInputHandler<@NotNull FluidStack> getInputHandler(IExtendedFluidTank tank, CachedRecipe.OperationTracker.RecipeError notEnoughError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
        return new IInputHandler<>() {

            @NotNull
            @Override
            public FluidStack getInput() {
                return tank.getFluid();
            }

            @NotNull
            @Override
            public FluidStack getRecipeInput(InputIngredient<@NotNull FluidStack> recipeIngredient) {
                FluidStack input = getInput();
                if (input.isEmpty()) {
                    //All recipes currently require that we have an input. If we don't then return that we failed
                    return FluidStack.EMPTY;
                }
                return recipeIngredient.getMatchingInstance(input);
            }

            @Override
            public void use(FluidStack recipeInput, int operations) {
                if (operations == 0 || recipeInput.isEmpty()) {
                    //Just exit if we are somehow here at zero operations
                    // or if something went wrong, this if should never really be true if we got to finishProcessing
                    return;
                }
                FluidStack inputFluid = getInput();
                if (!inputFluid.isEmpty()) {
                    int amount = recipeInput.getAmount() * operations;
                    logMismatchedStackSize(tank.shrinkStack(amount, Action.EXECUTE), amount);
                }
            }

            @Override
            public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, FluidStack recipeInput, int usageMultiplier) {
                //Only calculate if we need to use anything
                if (usageMultiplier > 0) {
                    //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
                    // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
                    // where we may want to allow not having the input be required for recipe matching
                    if (!recipeInput.isEmpty()) {
                        //TODO: Simulate the drain?
                        int operations = getInput().getAmount() / (recipeInput.getAmount() * usageMultiplier);
                        if (operations > 0) {
                            tracker.updateOperations(operations);
                            return;
                        }
                    }
                    // Not enough input to match the recipe, reset the progress
                    tracker.resetProgress(notEnoughError);
                }
            }
        };
    }

    private static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
        }
    }

}