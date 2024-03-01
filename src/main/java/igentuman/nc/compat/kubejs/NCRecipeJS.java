package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.recipe.ItemMatch;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import net.minecraft.world.item.ItemStack;

public class NCRecipeJS extends RecipeJS {

    public boolean hasOutput(ReplacementMatch match) {
        if (CommonProperties.get().matchJsonRecipes && match instanceof ItemMatch m) {
            if (this.getOriginalRecipe() != null) {
                ItemStack result = ItemStack.EMPTY;
                //ItemStack result = this.getResultItem(UtilsJS.staticRegistryAccess);
                return result != null && result != ItemStack.EMPTY && !result.isEmpty() && m.contains(result);
            }
        }

        return false;
    }

}
