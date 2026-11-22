package igentuman.nc.datagen.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;

/**
 * Static registry context for the recipe generators, bound by {@link ModRecipeProvider} for the
 * duration of {@code buildRecipes()}; tag ingredients and crafting builders need a {@link HolderGetter}.
 */
public final class RecipeGen {

    private static HolderGetter<Item> items;
    private static HolderGetter<Fluid> fluids;

    private RecipeGen() {}

    static void bind(HolderLookup.Provider registries) {
        items = registries.lookupOrThrow(Registries.ITEM);
        fluids = registries.lookupOrThrow(Registries.FLUID);
    }

    static void unbind() {
        items = null;
        fluids = null;
    }

    public static HolderGetter<Item> items() {
        return Objects.requireNonNull(items, "Recipe registry context is not bound");
    }

    public static HolderGetter<Fluid> fluids() {
        return Objects.requireNonNull(fluids, "Recipe registry context is not bound");
    }

    // --- ids ---

    /** Recipe key in the {@code nuclearcraft} namespace. */
    public static ResourceKey<Recipe<?>> key(String path) {
        return key(Identifier.fromNamespaceAndPath(MODID, path));
    }

    public static ResourceKey<Recipe<?>> key(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    // --- ingredients ---

    public static Ingredient ingredient(TagKey<Item> tag) {
        return Ingredient.of(items().getOrThrow(tag));
    }

    public static Ingredient ingredient(ItemLike item) {
        return Ingredient.of(item);
    }

    public static Ingredient ingredient(ItemLike... items) {
        return Ingredient.of(items);
    }

    public static SizedIngredient sized(TagKey<Item> tag, int count) {
        return new SizedIngredient(ingredient(tag), count);
    }

    public static SizedFluidIngredient fluidIngredient(TagKey<Fluid> tag, int amount) {
        return new SizedFluidIngredient(FluidIngredient.of(fluids().getOrThrow(tag)), amount);
    }

    // --- crafting builders ---

    public static ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item) {
        return ShapedRecipeBuilder.shaped(items(), category, item);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item, int count) {
        return ShapedRecipeBuilder.shaped(items(), category, item, count);
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item) {
        return ShapelessRecipeBuilder.shapeless(items(), category, item);
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item, int count) {
        return ShapelessRecipeBuilder.shapeless(items(), category, item, count);
    }

    /** Furnace recipe; every generated smelting recipe uses the {@code misc} book category. */
    public static SimpleCookingRecipeBuilder smelting(Ingredient input, RecipeCategory category, ItemLike result, float xp, int time) {
        return SimpleCookingRecipeBuilder.smelting(input, category, CookingBookCategory.MISC, result, xp, time);
    }

    /** Blast-furnace recipe; every generated blasting recipe uses the {@code misc} book category. */
    public static SimpleCookingRecipeBuilder blasting(Ingredient input, RecipeCategory category, ItemLike result, float xp, int time) {
        return SimpleCookingRecipeBuilder.blasting(input, category, CookingBookCategory.MISC, result, xp, time);
    }

    // --- unlock criteria ---

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items(), item).build());
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items(), tag).build());
    }
}
