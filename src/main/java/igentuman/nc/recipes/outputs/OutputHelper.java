package igentuman.nc.recipes.outputs;

import igentuman.nc.recipes.fluid.IExtendedFluidTank;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.functions.AutomationType;
import igentuman.nc.util.inventory.Action;
import igentuman.nc.util.inventory.IInventorySlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class OutputHelper {

    private OutputHelper() {
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


}