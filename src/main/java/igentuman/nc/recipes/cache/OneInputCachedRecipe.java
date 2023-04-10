package igentuman.nc.recipes.cache;

import igentuman.nc.recipes.FluidToFluidRecipe;
import igentuman.nc.recipes.ItemStackToFluidRecipe;
import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.recipes.inputs.IInputHandler;
import igentuman.nc.recipes.outputs.IOutputHandler;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Base class to help implement handling of recipes with two inputs.
 */
@NothingNullByDefault
public class OneInputCachedRecipe<INPUT, OUTPUT, RECIPE extends NcRecipe & Predicate<INPUT>> extends CachedRecipe<RECIPE> {

    private final IInputHandler<INPUT> inputHandler;
    private final IOutputHandler<OUTPUT> outputHandler;
    private final Predicate<INPUT> inputEmptyCheck;
    private final Supplier<? extends InputIngredient<INPUT>> inputSupplier;
    private final Function<INPUT, OUTPUT> outputGetter;
    private final Predicate<OUTPUT> outputEmptyCheck;

    //Note: Our input and output shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private INPUT input;
    @Nullable
    private OUTPUT output;

    /**
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     * @param inputSupplier    Supplier of the recipe's input ingredient.
     * @param outputGetter     Gets the recipe's output when given the corresponding input.
     * @param inputEmptyCheck  Checks if the input is empty.
     * @param outputEmptyCheck Checks if the output is empty (indicating something went horribly wrong).
     */
    protected OneInputCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<INPUT> inputHandler, IOutputHandler<OUTPUT> outputHandler,
                                   Supplier<? extends InputIngredient<INPUT>> inputSupplier, Function<INPUT, OUTPUT> outputGetter, Predicate<INPUT> inputEmptyCheck,
                                   Predicate<OUTPUT> outputEmptyCheck) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input ingredient supplier cannot be null.");
        this.outputGetter = Objects.requireNonNull(outputGetter, "Output getter cannot be null.");
        this.inputEmptyCheck = Objects.requireNonNull(inputEmptyCheck, "Input empty check cannot be null.");
        this.outputEmptyCheck = Objects.requireNonNull(outputEmptyCheck, "Output empty check cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, inputHandler, inputSupplier, input -> this.input = input, outputHandler, outputGetter,
              output -> this.output = output, inputEmptyCheck);
    }

    @Override
    public boolean isInputValid() {
        INPUT input = inputHandler.getInput();
        return !inputEmptyCheck.test(input) && recipe.test(input);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (input != null && output != null && !inputEmptyCheck.test(input) && !outputEmptyCheck.test(output)) {
            inputHandler.use(input, operations);
            outputHandler.handleOutput(output, operations);
        }
    }

    /**
     * Base implementation for handling Fluid to Fluid Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull FluidStack, @NotNull FluidStack, FluidToFluidRecipe> fluidToFluid(FluidToFluidRecipe recipe,
                                                                                                                  BooleanSupplier recheckAllErrors, IInputHandler<@NotNull FluidStack> inputHandler, IOutputHandler<@NotNull FluidStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, FluidStack::isEmpty,
              FluidStack::isEmpty);
    }

    /**
     * Base implementation for handling ItemStack to ItemStack Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull ItemStack, @NotNull ItemStack, ItemStackToItemStackRecipe> itemToItem(ItemStackToItemStackRecipe recipe,
                                                                                                                      BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull ItemStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ItemStack::isEmpty,
              ItemStack::isEmpty);
    }


    /**
     * Base implementation for handling ItemStack to Fluid Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull ItemStack, @NotNull FluidStack, ItemStackToFluidRecipe> itemToFluid(ItemStackToFluidRecipe recipe,
                                                                                                                    BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull FluidStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ItemStack::isEmpty,
              FluidStack::isEmpty);
    }

}