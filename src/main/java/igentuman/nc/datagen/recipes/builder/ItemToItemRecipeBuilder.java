package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ItemToItemRecipeBuilder extends NcRecipeBuilder<ItemToItemRecipeBuilder> {

    private final Ingredient input;
    private final ItemStack output;

    private double timeModifier;

    private double radiation;

    private double powerModifier;

    protected ItemToItemRecipeBuilder(String id, Ingredient input, ItemStack output) {
        super(ncSerializer(id));
        this.input = input;
        this.output = output;
        this.timeModifier = 1.0;
        this.radiation = 0.0;
        this.powerModifier = 1.0;
    }

    protected ItemToItemRecipeBuilder(String id, Ingredient input, ItemStack output, double timeModifier, double radiation, double powerModifier) {
        super(ncSerializer(id));
        this.input = input;
        this.output = output;
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
    }

    protected ItemToItemRecipeBuilder(String id, Ingredient input, ItemStack output, double timeModifier, double radiation) {
        super(ncSerializer(id));
        this.input = input;
        this.output = output;
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = 1.0;
    }

    protected ItemToItemRecipeBuilder(String id, NcIngredient input, ItemStack output, double timeModifier) {
        super(ncSerializer(id));
        this.input = input;
        this.output = output;
        this.timeModifier = timeModifier;
        this.radiation = 0.0;
        this.powerModifier = 1.0;
    }

    public static ItemToItemRecipeBuilder create(String id, Ingredient mainInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output item is empty.");
        }
        return new ItemToItemRecipeBuilder(id, mainInput,  output);
    }

    public static ItemToItemRecipeBuilder create(String id, ItemStack mainInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output item is empty.");
        }
        return new ItemToItemRecipeBuilder(id, Ingredient.of(mainInput),  output);
    }



    public static ItemToItemRecipeBuilder create(String id, Ingredient inputItem, Item outputItem) {
        ItemStack output = new ItemStack(outputItem);
        return new ItemToItemRecipeBuilder(id, inputItem,  output);
    }

    public ItemToItemRecipeBuilder modifiers(double timeModifier, double radiation, double powerModifier) {
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
        return this;
    }

    @Override
    protected FissionRecipeResult getResult(ResourceLocation id) {
        return new FissionRecipeResult(id);
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, output.getItem());
    }

    public class FissionRecipeResult extends RecipeResult {

        protected FissionRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add("input", serializeIngredient(input));
            json.add("output", serializeItemStack(output));
            json.addProperty("timeModifier", timeModifier);
            json.addProperty("radiation", radiation);
            json.addProperty("powerModifier", powerModifier);
        }
    }
}