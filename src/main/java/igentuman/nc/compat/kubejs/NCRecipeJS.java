package igentuman.nc.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

public class NCRecipeJS extends RecipeJS {
    public ArrayList<FluidStackIngredient> inputFluids = new ArrayList<>();
    public ArrayList<FluidStackJS> outputFluids = new ArrayList<>();
    public ArrayList<ItemStackIngredient> inputItems = new ArrayList<>();
    public ArrayList<ItemStackJS> outputItems = new ArrayList<>();

    @Override
    public void create(ListJS listJS) {

    }

    @Override
    public void deserialize() {
        inputItems.clear();
        outputItems.clear();
        inputFluids.clear();
        outputFluids.clear();
        try {
            if (json.has(JsonConstants.INPUT)) {
                if (GsonHelper.isArrayNode(json, JsonConstants.INPUT)) {
                    JsonElement input = GsonHelper.getAsJsonArray(json, JsonConstants.INPUT);
                    for (JsonElement in : input.getAsJsonArray()) {
                        inputItems.add(IngredientCreatorAccess.item().deserialize(in));
                    }
                } else {
                    JsonElement inputJson = GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
                    inputItems.add(IngredientCreatorAccess.item().deserialize(inputJson));
                }
            }
        } catch (Exception ignored) {    }

        try {
            if(json.has(JsonConstants.OUTPUT)) {
                if (GsonHelper.isArrayNode(json, JsonConstants.OUTPUT)) {
                    JsonElement output = GsonHelper.getAsJsonArray(json, JsonConstants.OUTPUT);
                    for (JsonElement out : output.getAsJsonArray()) {
                        try {
                            outputItems.add(ItemStackJS.of(SerializerHelper.getItemStack(out.getAsJsonObject())));
                        } catch (JsonSyntaxException ignored) {       }
                    }
                } else {
                    JsonElement output = GsonHelper.getAsJsonObject(json, JsonConstants.OUTPUT);
                    outputItems.add(ItemStackJS.of(SerializerHelper.getItemStack(output.getAsJsonObject())));
                }
            }
        } catch (Exception ignored) {    }

        try {
            if(json.has("inputFluids")) {
                if (GsonHelper.isArrayNode(json, "inputFluids")) {
                    JsonElement input = GsonHelper.getAsJsonArray(json, "inputFluids");
                    for (JsonElement in : input.getAsJsonArray()) {
                        inputFluids.add(IngredientCreatorAccess.fluid().deserialize(in));
                    }
                } else {
                    JsonElement inputJson = GsonHelper.getAsJsonObject(json, "inputFluids");
                    inputFluids.add(IngredientCreatorAccess.fluid().deserialize(inputJson));
                }
            }
        } catch (Exception ignored) {   }

        try {
            if(json.has("outputFluids")) {
                if (GsonHelper.isArrayNode(json, "outputFluids")) {
                    JsonElement output = GsonHelper.getAsJsonArray(json, "outputFluids");
                    for (JsonElement out : output.getAsJsonArray()) {
                        outputFluids.add(FluidStackJS.of(SerializerHelper.getFluidStack(out.getAsJsonObject())));
                    }
                } else {
                    JsonElement output = GsonHelper.getAsJsonObject(json, "outputFluids");
                    outputFluids.add(FluidStackJS.of(SerializerHelper.getFluidStack(output.getAsJsonObject(), "outputFluids")));
                }
            }
        } catch (Exception ignored) {  }
        double timeModifier = 1D;
        double powerModifier = 1D;
        double radiation = 1D;
        double rarityModifier = 1D;
        double temperature = 1D;
        try {
            timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
            powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
            radiation = GsonHelper.getAsDouble(json, "radiation", 1.0);
            rarityModifier = GsonHelper.getAsDouble(json, "rarityModifier", 1.0);
            temperature = GsonHelper.getAsDouble(json, "temperature", 1.0);
            if (temperature > 1) {
                rarityModifier = temperature;
            }
        } catch (Exception ignored) {   }
    }

    @Override
    public void serialize() {
        JsonArray inputItemsJson = new JsonArray();
        JsonArray inputFluidsJson = new JsonArray();
        JsonArray outputItemsJson = new JsonArray();
        JsonArray outputFluidsJson = new JsonArray();

        for (ItemStackIngredient ingredient : inputItems) {
            inputItemsJson.add(ingredient.serialize());
        }
        json.add("input", inputItemsJson);

        for (FluidStackIngredient fluid : inputFluids) {
            inputFluidsJson.add(fluid.serialize());
        }
        json.add("inputFluids", inputFluidsJson);

        for (ItemStackJS ingredient : outputItems) {
            outputItemsJson.add(ingredient.toJson());
        }
        json.add("output", outputItemsJson);

        for (FluidStackJS ingredient : outputFluids) {
            outputFluidsJson.add(ingredient.toJson());
        }
        json.add("outputFluids", outputFluidsJson);
    }
}
