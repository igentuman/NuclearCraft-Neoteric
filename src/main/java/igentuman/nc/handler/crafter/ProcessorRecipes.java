package igentuman.nc.handler.crafter;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

public final class ProcessorRecipes {
    private ProcessorRecipes() {}

    public static boolean isAllowed(String processor) {
        return processor != null && !processor.equals(Processors.PUMP) && !processor.equals(Processors.LEACHER)
                && Processors.all().containsKey(processor) && Processors.all().get(processor).hasRecipes();
    }

    @Nullable
    public static NcRecipe byId(RecipeManager manager, ResourceLocation id) {
        if (id == null) return null;
        Recipe<?> recipe = manager.byKey(id).orElse(null);
        return recipe instanceof NcRecipe r && !r.isIncomplete() && isAllowed(r.getCodeId()) ? r : null;
    }

    @Nullable
    public static ResourceLocation idOf(RecipeManager manager, NcRecipe recipe) {
        return byId(manager, recipe.getId()) == null ? null : recipe.getId();
    }

    @Nullable
    public static NcRecipe matchByItems(RecipeManager manager, List<ItemStack> ghosts) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : ghosts) if (!stack.isEmpty()) items.add(stack);
        if (items.isEmpty()) return null;
        NcRecipe best = null;
        for (Recipe<?> candidate : manager.getRecipes()) {
            if (!(candidate instanceof NcRecipe recipe) || !isAllowed(recipe.getCodeId()) || recipe.isIncomplete()) continue;
            if (recipe.getInputItems().length != items.size()) continue;
            if (!assign(recipe.getInputItems(), items, 0, new boolean[items.size()])) continue;
            if (best == null || recipe.getInputFluids().length < best.getInputFluids().length) best = recipe;
            if (best.getInputFluids().length == 0) return best;
        }
        return best;
    }

    private static boolean assign(ItemStackIngredient[] ingredients, List<ItemStack> items, int index, boolean[] used) {
        if (index == ingredients.length) return true;
        for (int i = 0; i < items.size(); i++) {
            if (used[i] || !ingredients[index].testType(items.get(i))) continue;
            used[i] = true;
            if (assign(ingredients, items, index + 1, used)) return true;
            used[i] = false;
        }
        return false;
    }
}
