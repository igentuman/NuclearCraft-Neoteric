package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

/** EMI integration helper that opens the recipe view for a given workstation stack. */
public class EmiHelper {

    public static void displayRecipes(ItemStack workstation) {
        EmiApi.displayRecipes(EmiStack.of(workstation));
    }
}
