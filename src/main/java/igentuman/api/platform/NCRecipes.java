package igentuman.api.platform;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Platform translation layer for NeoForge 1.21.1 recipe APIs.
 *
 * <p>NeoForge 1.21.1 wraps all recipes in {@link RecipeHolder}, moving the recipe ID
 * from the recipe object to the holder. This class handles the unwrapping and ID injection
 * so that NC's internal code can continue working with bare recipe objects.
 *
 * <h3>RecipeManager queries</h3>
 * <ul>
 *   <li>{@code recipeManager.getAllRecipesFor(type)} → {@link #getAllRecipesFor(RecipeManager, RecipeType)}</li>
 *   <li>{@code recipeManager.byKey(id)} → {@link #byKey(RecipeManager, ResourceLocation)}</li>
 * </ul>
 */
public final class NCRecipes {

    private NCRecipes() {}

    /**
     * Get all recipes of a type, unwrapping RecipeHolder and injecting IDs into AbstractRecipe.
     *
     * <p>Replaces {@code recipeManager.getAllRecipesFor(type)} which now returns
     * {@code List<RecipeHolder<T>>} instead of {@code List<T>}.
     */
    public static <T extends NcRecipe> List<T> getAllRecipesFor(RecipeManager manager, RecipeType<T> type) {
        return manager.getAllRecipesFor(type).stream()
                .map(holder -> {
                    T recipe = holder.value();
                    if (recipe instanceof AbstractRecipe ar) {
                        ar.setId(holder.id());
                    }
                    return recipe;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all vanilla recipes of a type, returning raw holders.
     *
     * <p>Used for vanilla recipe types (e.g., smelting) where recipes don't extend AbstractRecipe.
     */
    public static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getVanillaRecipes(
            RecipeManager manager, RecipeType<T> type) {
        return manager.getAllRecipesFor(type);
    }

    /**
     * Find a recipe by its ID, unwrapping the RecipeHolder.
     *
     * <p>Wraps {@code recipeManager.byKey(id)} which in 1.21.1 returns
     * {@code Optional<RecipeHolder<?>>} instead of {@code Optional<Recipe<?>>}.
     */
    public static Optional<RecipeHolder<?>> byKey(RecipeManager manager, ResourceLocation id) {
        return manager.byKey(id);
    }
}
