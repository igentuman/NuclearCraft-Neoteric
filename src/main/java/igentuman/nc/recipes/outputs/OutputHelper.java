package igentuman.nc.recipes.outputs;

import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.recipes.fluid.IExtendedFluidTank;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.functions.AutomationType;
import igentuman.nc.util.inventory.Action;
import igentuman.nc.util.inventory.IInventorySlot;
import igentuman.nc.util.math.MathUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NothingNullByDefault
public class OutputHelper {

    private OutputHelper() {
    }


    /**
     * Wrap a fluid tank into an {@link IOutputHandler}.
     *
     * @param tank                Tank to wrap.
     * @param notEnoughSpaceError The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NotNull FluidStack> getOutputHandler(IExtendedFluidTank tank, CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(tank, "Tank cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(FluidStack toOutput, int operations) {
                OutputHelper.handleOutput(tank, toOutput, operations);
            }

            @Override
            public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, FluidStack toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, tank, toOutput);
            }
        };
    }

    /**
     * Wrap an inventory slot into an {@link IOutputHandler}.
     *
     * @param slot                Slot to wrap.
     * @param notEnoughSpaceError The error to apply if the output causes the recipe to not be able to perform any operations.
     */
    public static IOutputHandler<@NotNull ItemStack> getOutputHandler(IInventorySlot slot, CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError) {
        Objects.requireNonNull(slot, "Slot cannot be null.");
        Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
        return new IOutputHandler<>() {

            @Override
            public void handleOutput(ItemStack toOutput, int operations) {
                OutputHelper.handleOutput(slot, toOutput, operations);
            }

            @Override
            public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, ItemStack toOutput) {
                OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, slot, toOutput);
            }
        };
    }

    private static void handleOutput(IExtendedFluidTank fluidTank, FluidStack toOutput, int operations) {
        if (operations == 0) {
            //This should not happen
            return;
        }
        fluidTank.insert(new FluidStack(toOutput, toOutput.getAmount() * operations), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private static void handleOutput(IInventorySlot inventorySlot, ItemStack toOutput, int operations) {
        if (operations == 0 || toOutput.isEmpty()) {
            return;
        }
        ItemStack output = toOutput.copy();
        if (operations > 1) {
            //If we are doing more than one operation we need to make a copy of our stack and change the amount
            // that we are using the fill the tank with
            output.setCount(output.getCount() * operations);
        }
        inventorySlot.insertItem(output, Action.EXECUTE, AutomationType.INTERNAL);
    }



    private static void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, CachedRecipe.OperationTracker.RecipeError notEnoughSpace, IExtendedFluidTank tank, FluidStack toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (!toOutput.isEmpty()) {
            //Copy the stack and make it be max size
            FluidStack maxOutput = new FluidStack(toOutput, Integer.MAX_VALUE);
            //Then simulate filling the fluid tank, so we can see how much actually can fit
            FluidStack remainder = tank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
            int amountUsed = maxOutput.getAmount() - remainder.getAmount();
            //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
            int operations = amountUsed / toOutput.getAmount();
            tracker.updateOperations(operations);
            if (operations == 0) {
                if (amountUsed == 0 && tank.getNeeded() > 0) {
                    tracker.addError(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                } else {
                    tracker.addError(notEnoughSpace);
                }
            }
        }
    }

    private static void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, CachedRecipe.OperationTracker.RecipeError notEnoughSpace, IInventorySlot slot, ItemStack toOutput) {
        //If our output is empty, we have nothing to add, so we treat it as being able to fit all
        if (!toOutput.isEmpty()) {
            ItemStack output = toOutput.copy();
            //Make a cope of the stack we are outputting with its maximum size
            output.setCount(output.getMaxStackSize());
            ItemStack remainder = slot.insertItem(output, Action.SIMULATE, AutomationType.INTERNAL);
            int amountUsed = output.getCount() - remainder.getCount();
            //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
            int operations = amountUsed / toOutput.getCount();
            tracker.updateOperations(operations);
            if (operations == 0) {
                if (amountUsed == 0 && slot.getLimit(slot.getStack()) - slot.getCount() > 0) {
                    tracker.addError(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                } else {
                    tracker.addError(notEnoughSpace);
                }
            }
        }
    }

}