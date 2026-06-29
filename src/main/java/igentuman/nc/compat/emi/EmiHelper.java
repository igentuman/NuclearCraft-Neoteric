package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

public class EmiHelper {

    public static void displayRecipes(ItemStack workstation) {
        EmiApi.displayRecipes(EmiStack.of(workstation));
    }
}
