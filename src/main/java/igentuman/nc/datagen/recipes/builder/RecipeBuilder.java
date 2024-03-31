package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraft.advancements.IRequirementsStrategy.OR;

public abstract class RecipeBuilder<BUILDER extends RecipeBuilder<BUILDER>> {

    protected static ResourceLocation ncSerializer(String name) {
        return new ResourceLocation(MODID, name);
    }

    protected final List<ICondition> conditions = new ArrayList<>();
    protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
    protected final ResourceLocation serializerName;

    protected RecipeBuilder(ResourceLocation serializerName) {
        this.serializerName = serializerName;
    }

    /**
     * Adds a criterion to this recipe.
     *
     * @param criterion Criterion to add.
     */
/*    public BUILDER addCriterion(Criterit criterion) {
        return addCriterion(criterion.name(), criterion.criterion());
    }*/

    /**
     * Adds a criterion to this recipe.
     *
     * @param name      Name of the criterion.
     * @param criterion Criterion to add.
     */
    public BUILDER addCriterion(String name, ICriterionInstance criterion) {
        advancementBuilder.addCriterion(name, criterion);
        return (BUILDER) this;
    }

    /**
     * Adds a condition to this recipe.
     *
     * @param condition Condition to add.
     */
    public BUILDER addCondition(ICondition condition) {
        conditions.add(condition);
        return (BUILDER) this;
    }

    /**
     * Checks if this recipe has any criteria.
     *
     * @return {@code true} if this recipe has any criteria.
     */
    protected boolean hasCriteria() {
        return !advancementBuilder.getCriteria().isEmpty();
    }

    /**
     * Gets a recipe result object.
     *
     * @param id ID of the recipe being built.
     */
    protected abstract RecipeResult getResult(ResourceLocation id);

    /**
     * Performs any extra validation.
     *
     * @param id ID of the recipe validation is being performed on.
     */
    protected void validate(ResourceLocation id) {
    }

    /**
     * Builds this recipe.
     *
     * @param consumer Finished Recipe Consumer.
     * @param id       Name of the recipe being built.
     */
    public void build(Consumer<IFinishedRecipe> consumer, ResourceLocation id) {
        validate(id);
        if (hasCriteria()) {
            //If there is a way to "unlock" this recipe then add an advancement with the criteria
            advancementBuilder.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                  .rewards(AdvancementRewards.Builder.recipe(id)).requirements(OR);
        }
        consumer.accept(getResult(id));
    }

    /**
     * Builds this recipe basing the name on the output item.
     *
     * @param consumer Finished Recipe Consumer.
     * @param output       Output to base the recipe name off of.
     */
    protected void build(Consumer<IFinishedRecipe> consumer, IItemProvider... output) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(output[0].asItem());
        if (registryName == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        build(consumer, registryName);
    }

    /**
     * Base recipe result.
     */
    protected abstract class RecipeResult implements IFinishedRecipe {

        private final ResourceLocation id;

        public RecipeResult(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", serializerName.toString());
            if (!conditions.isEmpty()) {
                JsonArray conditionsArray = new JsonArray();
                for (ICondition condition : conditions) {
                    conditionsArray.add(CraftingHelper.serialize(condition));
                }
                jsonObject.add("conditions", conditionsArray);
            }
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @NotNull
        @Override
        public IRecipeSerializer<?> getType() {
            return ForgeRegistries.RECIPE_SERIALIZERS.getValue(serializerName);
        }

        @NotNull
        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return hasCriteria() ? advancementBuilder.serializeToJson() : null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath());
        }
    }
    public static JsonElement serializeItemStack(@NotNull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            if(stack.getTag().contains("Damage")) {
                if(stack.getTag().getInt("Damage") == 0) {
                    stack.getTag().remove("Damage");
                }
            }
            if(!stack.getTag().getAllKeys().isEmpty())
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    public static JsonElement serializeIngredient(@NotNull Ingredient ingredient) {
        return ingredient.toJson();
    }


    public static JsonElement serializeFluidStack(@NotNull FluidStack fluidStack) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString());
        json.addProperty("amount", fluidStack.getAmount());
        if (fluidStack.hasTag()) {
            if(fluidStack.getTag().contains("Damage")) {
                if(fluidStack.getTag().getInt("Damage") == 0) {
                    fluidStack.getTag().remove("Damage");
                }
            }
            if(!fluidStack.getTag().getAllKeys().isEmpty())
            json.addProperty("nbt", fluidStack.getTag().toString());
        }
        return json;
    }
}