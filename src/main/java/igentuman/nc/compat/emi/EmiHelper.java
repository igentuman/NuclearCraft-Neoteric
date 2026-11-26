package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

/** EMI integration helper that opens the recipe view for a given workstation stack. */
public class EmiHelper {

    /** Generated displays are not recipe-manager entries; EMI marks them with a leading slash. */
    public static ResourceLocation syntheticRecipeId(EmiRecipeCategory category, ResourceLocation source) {
        return ResourceLocation.fromNamespaceAndPath(category.getId().getNamespace(),
                "/" + category.getId().getPath() + "/" + source.getNamespace() + "/" + source.getPath());
    }

    public static void displayRecipes(ItemStack workstation) {
        EmiApi.displayRecipes(EmiStack.of(workstation));
    }
}
