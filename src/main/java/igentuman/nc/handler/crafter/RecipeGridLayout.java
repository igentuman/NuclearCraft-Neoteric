package igentuman.nc.handler.crafter;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

public final class RecipeGridLayout {

    private RecipeGridLayout() {}

    public static Ingredient[] layout(CraftingRecipe recipe) {
        Ingredient[] grid = new Ingredient[9];
        for (int i = 0; i < 9; i++) grid[i] = Ingredient.EMPTY;

        if (recipe instanceof ShapedRecipe shaped) {
            int w = shaped.getWidth();
            int h = shaped.getHeight();
            NonNullList<Ingredient> ing = shaped.getIngredients();
            for (int r = 0; r < h && r < 3; r++) {
                for (int c = 0; c < w && c < 3; c++) {
                    int src = r * w + c;
                    if (src < ing.size()) grid[r * 3 + c] = ing.get(src);
                }
            }
        } else {
            NonNullList<Ingredient> ing = recipe.getIngredients();
            int idx = 0;
            for (int i = 0; i < ing.size() && idx < 9; i++) {
                grid[idx++] = ing.get(i);
            }
        }
        return grid;
    }
}
