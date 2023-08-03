package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ItemToItemRecipeBuilder extends NcRecipeBuilder<ItemToItemRecipeBuilder> {

    private final List<NcIngredient> input;
    private final ItemStack[] output;

    private double timeModifier;

    private double radiation;

    private double powerModifier;

    protected ItemToItemRecipeBuilder(String id, NcIngredient input, ItemStack output) {
        super(ncSerializer(id));
        this.input = List.of(input);
        this.output = new ItemStack[]{output};
        this.timeModifier = 1.0;
        this.radiation = 0.0;
        this.powerModifier = 1.0;
    }

    protected ItemToItemRecipeBuilder(String id, NcIngredient input, ItemStack output, double timeModifier, double radiation, double powerModifier) {
        super(ncSerializer(id));
        this.input = List.of(input);
        this.output = new ItemStack[]{output};
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
    }

    protected ItemToItemRecipeBuilder(String id, NcIngredient input, ItemStack output, double timeModifier, double radiation) {
        super(ncSerializer(id));
        this.input = List.of(input);
        this.output = new ItemStack[]{output};
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = 1.0;
    }

    protected ItemToItemRecipeBuilder(String id, NcIngredient input, ItemStack output, double timeModifier) {
        super(ncSerializer(id));
        this.input = List.of(input);
        this.output = new ItemStack[]{output};
        this.timeModifier = timeModifier;
        this.radiation = 0.0;
        this.powerModifier = 1.0;
    }

    protected ItemToItemRecipeBuilder(String id,  List<NcIngredient> input, ItemStack output, double timeModifier) {
        super(ncSerializer(id));
        this.input = input;
        this.output = new ItemStack[]{output};
        this.timeModifier = timeModifier;
        this.radiation = 0.0;
        this.powerModifier = 1.0;
    }

    protected ItemToItemRecipeBuilder(String id, List<NcIngredient> input, ItemStack[] output) {
        super(ncSerializer(id));
        this.input = input;
        this.output = output;
        this.timeModifier = 1.0;
        this.radiation = 0.0;
        this.powerModifier = 1.0;
    }

    public static ItemToItemRecipeBuilder create(String id, NcIngredient mainInput, NcIngredient mainInput2,  ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output item is empty.");
        }
        return new ItemToItemRecipeBuilder(id, List.of(mainInput, mainInput2), output, 1);
    }

    public static ItemToItemRecipeBuilder create(String id, NcIngredient mainInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output item is empty.");
        }
        return new ItemToItemRecipeBuilder(id, mainInput,  output);
    }



    public static ItemToItemRecipeBuilder create(String id, NcIngredient inputItem, Item outputItem) {
        ItemStack output = new ItemStack(outputItem);
        return new ItemToItemRecipeBuilder(id, inputItem,  output);
    }

    public static ItemToItemRecipeBuilder create(String id, NcIngredient stack, ItemStack[] itemStacks) {
        return new ItemToItemRecipeBuilder(id, List.of(stack), itemStacks);
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
        build(consumer, output[0].getItem());
    }

    public class FissionRecipeResult extends RecipeResult {

        protected FissionRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            JsonArray inputJson = new JsonArray();
            for(Ingredient in: input) {
                inputJson.add(serializeIngredient(in));
            }
            json.add("input", inputJson);

            JsonArray outJson = new JsonArray();
            for (ItemStack out: output) {
                outJson.add(serializeItemStack(out));
            }
            json.add("output", outJson);

            json.addProperty("timeModifier", timeModifier);
            json.addProperty("radiation", radiation);
            json.addProperty("powerModifier", powerModifier);
        }
    }
}