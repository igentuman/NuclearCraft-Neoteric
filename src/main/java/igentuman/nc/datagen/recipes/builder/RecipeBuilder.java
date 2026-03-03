package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.api.platform.NCRecipeOutput;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCFluidStacks;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.NcUtils.rlFromString;

public abstract class RecipeBuilder<BUILDER extends RecipeBuilder<BUILDER>> {

    protected static ResourceLocation ncSerializer(String name) {
        return rl(name);
    }

    protected final List<ICondition> conditions = new ArrayList<>();
    protected final ResourceLocation serializerName;
    private boolean hasCriterion = false;
    private Advancement.Builder advancementBuilder;

    protected RecipeBuilder(ResourceLocation serializerName) {
        this.serializerName = serializerName;
    }

    private Advancement.Builder getOrCreateAdvancement() {
        if (advancementBuilder == null) {
            advancementBuilder = Advancement.Builder.advancement();
        }
        return advancementBuilder;
    }

    /**
     * Adds a criterion to this recipe.
     */
    public BUILDER addCriterion(RecipeCriterion criterion) {
        return addCriterion(criterion.name(), criterion.criterion());
    }

    /**
     * Adds a criterion to this recipe.
     */
    public BUILDER addCriterion(String name, Criterion<?> criterion) {
        getOrCreateAdvancement().addCriterion(name, criterion);
        hasCriterion = true;
        return (BUILDER) this;
    }

    /**
     * Adds a condition to this recipe.
     */
    public BUILDER addCondition(ICondition condition) {
        conditions.add(condition);
        return (BUILDER) this;
    }

    /**
     * Checks if this recipe has any criteria.
     */
    protected boolean hasCriteria() {
        return hasCriterion;
    }

    /**
     * Gets a recipe result object.
     */
    protected abstract RecipeResult getResult(ResourceLocation id);

    /**
     * Performs any extra validation.
     */
    protected void validate(ResourceLocation id) {
    }

    /**
     * Builds this recipe.
     */
    public void build(RecipeOutput output, ResourceLocation id) {
        validate(id);

        // Build the recipe JSON
        RecipeResult result = getResult(id);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", serializerName.toString());
        result.serializeRecipeData(jsonObject);

        // Build advancement if criteria exist
        AdvancementHolder advancement = null;
        if (hasCriteria()) {
            advancement = getOrCreateAdvancement()
                    .parent(rlFromString("recipes/root"))
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR)
                    .build(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "recipes/" + id.getPath()));
        }

        // Save via NCRecipeOutput's JSON path
        if (output instanceof NCRecipeOutput ncOutput) {
            ncOutput.acceptJson(id, jsonObject, advancement, conditions.toArray(ICondition[]::new));
        } else {
            // Fallback: embed conditions directly in JSON and use the standard path
            // This shouldn't normally happen, but provides safety
            if (!conditions.isEmpty()) {
                ICondition.writeConditions(com.mojang.serialization.JsonOps.INSTANCE, jsonObject, conditions);
            }
            // Can't use standard accept() without a real Recipe object — save via DataProvider
            throw new UnsupportedOperationException(
                    "NC custom recipe builders require NCRecipeOutput. Recipe: " + id);
        }
    }

    /**
     * Builds this recipe basing the name on the output item.
     */
    protected void build(RecipeOutput output, ItemLike... items) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(items[0].asItem());
        if (registryName == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        build(output, registryName);
    }

    /**
     * Base recipe result — produces JSON via serializeRecipeData().
     * No longer implements FinishedRecipe (removed in 1.21.1).
     */
    protected abstract class RecipeResult {

        private final ResourceLocation id;

        public RecipeResult(ResourceLocation id) {
            this.id = id;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        /**
         * Serializes the recipe-specific data into the JSON object.
         */
        public abstract void serializeRecipeData(@NotNull JsonObject json);
    }

    public static JsonElement serializeItemStack(@NotNull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        if (NCItemStacks.hasCustomData(stack)) {
            CompoundTag tag = NCItemStacks.getTag(stack);
            if(tag.contains("Damage")) {
                if(tag.getInt("Damage") == 0) {
                    tag.remove("Damage");
                }
            }
            if(!tag.getAllKeys().isEmpty())
            json.addProperty("nbt", tag.toString());
        }
        return json;
    }

    public static JsonElement serializeIngredient(@NotNull Ingredient ingredient) {
        return NCRecipeOutput.serializeIngredient(ingredient);
    }


    public static JsonElement serializeFluidStack(@NotNull FluidStack fluidStack) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString());
        json.addProperty("amount", fluidStack.getAmount());
        if (NCFluidStacks.hasCustomData(fluidStack)) {
            CompoundTag tag = NCFluidStacks.getTag(fluidStack);
            if(tag.contains("Damage")) {
                if(tag.getInt("Damage") == 0) {
                    tag.remove("Damage");
                }
            }
            if(!tag.getAllKeys().isEmpty())
            json.addProperty("nbt", tag.toString());
        }
        return json;
    }
}
