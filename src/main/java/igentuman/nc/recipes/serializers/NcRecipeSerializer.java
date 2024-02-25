package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;


import static igentuman.nc.recipes.NcRecipeSerializers.SERIALIZERS;
import static igentuman.nc.util.TagUtil.getItemsByTagKey;

public class NcRecipeSerializer<RECIPE extends NcRecipe> implements RecipeSerializer<RECIPE> {

    final IFactory<RECIPE> factory;

    public NcRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ItemStackIngredient[] inputItems = new ItemStackIngredient[0];
        try {
            if (json.has(JsonConstants.INPUT)) {
                if (GsonHelper.isArrayNode(json, JsonConstants.INPUT)) {
                    JsonElement input = GsonHelper.getAsJsonArray(json, JsonConstants.INPUT);
                    inputItems = new ItemStackIngredient[input.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement in : input.getAsJsonArray()) {
                        inputItems[i] = IngredientCreatorAccess.item().deserialize(in);
                        i++;
                    }
                } else {
                    JsonElement inputJson = GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
                    inputItems = new ItemStackIngredient[]{IngredientCreatorAccess.item().deserialize(inputJson)};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse input for recipe: "+recipeId);
        }

        ItemStackIngredient[] outputItems = new ItemStackIngredient[0];
        try {
            if(json.has(JsonConstants.OUTPUT)) {
                if (GsonHelper.isArrayNode(json, JsonConstants.OUTPUT)) {
                    JsonElement output = GsonHelper.getAsJsonArray(json, JsonConstants.OUTPUT);
                    outputItems = new ItemStackIngredient[output.getAsJsonArray().size()];

                    int i = 0;
                    for (JsonElement out : output.getAsJsonArray()) {
                        try {
                            outputItems[i] = IngredientCreatorAccess.item().deserialize(out);
                        } catch (JsonSyntaxException ex) {
                            NuclearCraft.LOGGER.error("Error parsing output itemstack for recipe: " + recipeId.toString());
                        }
                        i++;
                    }
                } else {
                    JsonElement output = GsonHelper.getAsJsonObject(json, JsonConstants.OUTPUT);
                    outputItems = new ItemStackIngredient[]{IngredientCreatorAccess.item().deserialize(output.getAsJsonObject())};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse output for recipe: "+recipeId);
        }

        FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
        try {
            if(json.has("inputFluids")) {
                if (GsonHelper.isArrayNode(json, "inputFluids")) {
                    JsonElement input = GsonHelper.getAsJsonArray(json, "inputFluids");
                    inputFluids = new FluidStackIngredient[input.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement in : input.getAsJsonArray()) {
                        inputFluids[i] = IngredientCreatorAccess.fluid().deserialize(in);
                        i++;
                    }
                } else {
                    JsonElement inputJson = GsonHelper.getAsJsonObject(json, "inputFluids");
                    inputFluids = new FluidStackIngredient[]{IngredientCreatorAccess.fluid().deserialize(inputJson)};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse input fluid for recipe: "+recipeId);
        }

        FluidStackIngredient[] outputFluids = new FluidStackIngredient[0];
        try {
            if(json.has("outputFluids")) {
                if (GsonHelper.isArrayNode(json, "outputFluids")) {
                    JsonElement output = GsonHelper.getAsJsonArray(json, "outputFluids");
                    outputFluids = new FluidStackIngredient[output.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement out : output.getAsJsonArray()) {
                        outputFluids[i] = IngredientCreatorAccess.fluid().deserialize(out.getAsJsonObject());
                        i++;
                    }
                } else {
                    JsonElement output = GsonHelper.getAsJsonObject(json, "outputFluids");
                    outputFluids = new FluidStackIngredient[]{IngredientCreatorAccess.fluid().deserialize(output.getAsJsonObject())};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse output fluid for recipe: "+recipeId);
        }
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
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse params for recipe: "+recipeId);
        }
        return this.factory.create(recipeId, inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, radiation, rarityModifier);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        if(recipeId.getPath().contains("nc_ore_veins")) {
            return (RECIPE) SERIALIZERS.get("nc_ore_veins").get().fromNetwork(recipeId, buffer);
        }
        try {

            int inputSize = buffer.readInt();
            ItemStackIngredient[] inputItems = new ItemStackIngredient[inputSize];
            for(int i = 0; i < inputSize; i++) {
                inputItems[i] = IngredientCreatorAccess.item().read(buffer);
            }

            int outputSize = buffer.readInt();
            ItemStackIngredient[] outputItems = new ItemStackIngredient[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputItems[i] =  IngredientCreatorAccess.item().read(buffer);
            }

            inputSize = buffer.readInt();
            FluidStackIngredient[] inputFluids = new FluidStackIngredient[inputSize];
            for(int i = 0; i < inputSize; i++) {
                inputFluids[i] = IngredientCreatorAccess.fluid().read(buffer);
            }

            outputSize = buffer.readInt();
            FluidStackIngredient[] outputFluids = new FluidStackIngredient[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputFluids[i] =  IngredientCreatorAccess.fluid().read(buffer);
            }

            double timeModifier = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();

            return this.factory.create(recipeId, inputItems, outputItems, inputFluids,  outputFluids, timeModifier, powerModifier, radiation, 1);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error reading recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error writing recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends NcRecipe> {
        RECIPE create(ResourceLocation id,
                      ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeMultiplier, double powerMultiplier, double radiationMultiplier, double rarityMultiplier);
    }
}