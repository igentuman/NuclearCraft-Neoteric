package igentuman.nc.handler.crafter;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

/** Maps placeable crafting ingredients to the terminal's top-left-aligned 3x3 grid. */
public final class RecipeGridLayout {

    private RecipeGridLayout() {}

    /** Null denotes a hole; recipes without a safe 3x3 placement have no layout. */
    public static @Nullable Ingredient[] layout(CraftingRecipe recipe) {
        PlacementInfo placement = recipe.placementInfo();
        if (placement.isImpossibleToPlace()) return null;
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
        int height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : 3;
        if (width < 1 || width > 3 || height < 1 || height > 3) return null;
        var slots = placement.slotsToIngredientIndex();
        if (slots.size() > width * height) return null;

        Ingredient[] grid = new Ingredient[9];
        for (int slot = 0; slot < slots.size(); slot++) {
            int ingredient = slots.getInt(slot);
            if (ingredient == PlacementInfo.EMPTY_SLOT) continue;
            if (ingredient < 0 || ingredient >= placement.ingredients().size()) return null;
            grid[(slot / width) * 3 + slot % width] = placement.ingredients().get(ingredient);
        }
        return grid;
    }
}
