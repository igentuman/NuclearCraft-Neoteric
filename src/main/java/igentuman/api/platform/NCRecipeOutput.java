package igentuman.api.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Platform wrapper bridging NC's JSON-serialized custom recipes to
 * NeoForge 1.21.1's RecipeOutput system.
 *
 * In 1.21.1, RecipeOutput.accept() expects actual Recipe<?> objects
 * that get serialized through codecs. NC's custom recipe builders
 * produce JSON directly. This class provides both paths:
 * - Standard accept() for vanilla recipe builders (Shaped/Shapeless/Smelting)
 * - acceptJson() for NC custom JSON-based recipe builders
 *
 * Also provides serializeIngredient() replacing the removed Ingredient.toJson().
 */
public class NCRecipeOutput implements RecipeOutput {

    private final CachedOutput cachedOutput;
    private final HolderLookup.Provider registries;
    private final PackOutput.PathProvider recipePath;
    private final PackOutput.PathProvider advancementPath;
    private final Set<ResourceLocation> seenRecipes = new HashSet<>();
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public NCRecipeOutput(CachedOutput cachedOutput, HolderLookup.Provider registries,
                          PackOutput.PathProvider recipePath, PackOutput.PathProvider advancementPath) {
        this.cachedOutput = cachedOutput;
        this.registries = registries;
        this.recipePath = recipePath;
        this.advancementPath = advancementPath;
    }

    // --- Standard RecipeOutput interface (for vanilla builders) ---

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe,
                       @Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (!seenRecipes.add(id)) {
            throw new IllegalStateException("Duplicate recipe " + id);
        }
        futures.add(DataProvider.saveStable(cachedOutput, registries,
                Recipe.CONDITIONAL_CODEC,
                Optional.of(new net.neoforged.neoforge.common.conditions.WithConditions<>(recipe, conditions)),
                recipePath.json(id)));
        if (advancement != null) {
            futures.add(DataProvider.saveStable(cachedOutput, registries,
                    Advancement.CONDITIONAL_CODEC,
                    Optional.of(new net.neoforged.neoforge.common.conditions.WithConditions<>(advancement.value(), conditions)),
                    advancementPath.json(advancement.id())));
        }
    }

    @Override
    public Advancement.Builder advancement() {
        return Advancement.Builder.recipeAdvancement()
                .parent(net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
    }

    // --- JSON-based recipe saving (for NC custom builders) ---

    /**
     * Saves a JSON-serialized recipe directly, bypassing the codec dispatch.
     * Used by NC's custom recipe builders that produce JSON via serializeRecipeData().
     *
     * @param id             Recipe resource location
     * @param recipeJson     Full recipe JSON (including "type" field)
     * @param advancement    Optional advancement holder
     * @param conditions     NeoForge conditions to embed in the JSON
     */
    public void acceptJson(ResourceLocation id, JsonObject recipeJson,
                           @Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (!seenRecipes.add(id)) {
            throw new IllegalStateException("Duplicate recipe " + id);
        }
        // Embed NeoForge conditions into the recipe JSON
        if (conditions.length > 0) {
            ICondition.writeConditions(registries, recipeJson, conditions);
        }
        futures.add(DataProvider.saveStable(cachedOutput, recipeJson, recipePath.json(id)));
        if (advancement != null) {
            futures.add(DataProvider.saveStable(cachedOutput, registries,
                    Advancement.CONDITIONAL_CODEC,
                    Optional.of(new net.neoforged.neoforge.common.conditions.WithConditions<>(advancement.value(), conditions)),
                    advancementPath.json(advancement.id())));
        }
    }

    /**
     * Returns a future that completes when all recipes have been saved.
     */
    public CompletableFuture<?> getFuture() {
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    // --- Static helpers replacing removed APIs ---

    /**
     * Replaces the removed Ingredient.toJson().
     * Uses Ingredient.CODEC to encode to JsonElement.
     */
    public static JsonElement serializeIngredient(Ingredient ingredient) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient)
                .getOrThrow(msg -> new IllegalStateException("Failed to serialize ingredient: " + msg));
    }

    /**
     * Replaces the removed CraftingHelper.serialize(ICondition).
     * Uses ICondition.CODEC to encode a single condition to JsonElement.
     */
    public static JsonElement serializeCondition(ICondition condition) {
        return ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition)
                .getOrThrow(msg -> new IllegalStateException("Failed to serialize condition: " + msg));
    }
}
