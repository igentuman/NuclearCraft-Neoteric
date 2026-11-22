package igentuman.nc.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/** Supplies server recipes or the recipe snapshot synchronized to the logical client. */
public final class RecipeSources {
    private static RecipeMap clientRecipes = RecipeMap.EMPTY;

    private RecipeSources() {}

    public static RecipeMap recipes(@Nullable Level level) {
        if (level instanceof ServerLevel serverLevel) return serverLevel.recipeAccess().recipeMap();
        return level != null && level.isClientSide() ? clientRecipes : RecipeMap.EMPTY;
    }

    public static Optional<RecipeHolder<?>> byId(@Nullable Level level, Identifier id) {
        return Optional.ofNullable(recipes(level).byKey(ResourceKey.create(Registries.RECIPE, id)));
    }

    /** Called only by client recipe-received and logout events; server lookups never use this field. */
    public static void replaceClientRecipes(RecipeMap recipes) {
        clientRecipes = recipes;
    }
}
