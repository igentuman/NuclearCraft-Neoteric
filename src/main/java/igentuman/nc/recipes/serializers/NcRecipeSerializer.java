package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;


public class NcRecipeSerializer<RECIPE extends NcRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    final IFactory<RECIPE> factory;
    private ResourceLocation regName;

    public NcRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ItemStackIngredient[] inputItems = new ItemStackIngredient[0];
        try {
            if (json.has(JsonConstants.INPUT)) {
                if (JSONUtils.isArrayNode(json, JsonConstants.INPUT)) {
                    JsonElement input = JSONUtils.getAsJsonArray(json, JsonConstants.INPUT);
                    inputItems = new ItemStackIngredient[input.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement in : input.getAsJsonArray()) {
                        inputItems[i] = IngredientCreatorAccess.item().deserialize(in);
                        i++;
                    }
                } else {
                    JsonElement inputJson = JSONUtils.getAsJsonObject(json, JsonConstants.INPUT);
                    inputItems = new ItemStackIngredient[]{IngredientCreatorAccess.item().deserialize(inputJson)};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse input for recipe: "+recipeId);
        }

        ItemStack[] outputItems = new ItemStack[0];
        try {
            if(json.has(JsonConstants.OUTPUT)) {
                if (JSONUtils.isArrayNode(json, JsonConstants.OUTPUT)) {
                    JsonElement output = JSONUtils.getAsJsonArray(json, JsonConstants.OUTPUT);
                    outputItems = new ItemStack[output.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement out : output.getAsJsonArray()) {
                        try {
                            outputItems[i] = SerializerHelper.getItemStack(out.getAsJsonObject());
                        } catch (JsonSyntaxException ex) {
                            NuclearCraft.LOGGER.error("Error parsing output itemstack for recipe: " + recipeId.toString());
                        }
                        i++;
                    }
                } else {
                    JsonElement output = JSONUtils.getAsJsonObject(json, JsonConstants.OUTPUT);
                    outputItems = new ItemStack[]{SerializerHelper.getItemStack(output.getAsJsonObject())};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse output for recipe: "+recipeId);
        }

        FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
        try {
            if(json.has("inputFluids")) {
                if (JSONUtils.isArrayNode(json, "inputFluids")) {
                    JsonElement input = JSONUtils.getAsJsonArray(json, "inputFluids");
                    inputFluids = new FluidStackIngredient[input.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement in : input.getAsJsonArray()) {
                        inputFluids[i] = IngredientCreatorAccess.fluid().deserialize(in);
                        i++;
                    }
                } else {
                    JsonElement inputJson = JSONUtils.getAsJsonObject(json, "inputFluids");
                    inputFluids = new FluidStackIngredient[]{IngredientCreatorAccess.fluid().deserialize(inputJson)};
                }
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse input fluid for recipe: "+recipeId);
        }

        FluidStack[] outputFluids = new FluidStack[0];
        try {
            if(json.has("outputFluids")) {
                if (JSONUtils.isArrayNode(json, "outputFluids")) {
                    JsonElement output = JSONUtils.getAsJsonArray(json, "outputFluids");
                    outputFluids = new FluidStack[output.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement out : output.getAsJsonArray()) {
                        outputFluids[i] = SerializerHelper.getFluidStack(out.getAsJsonObject());
                        i++;
                    }
                } else {
                    JsonElement output = JSONUtils.getAsJsonObject(json, "outputFluids");
                    outputFluids = new FluidStack[]{SerializerHelper.getFluidStack(output.getAsJsonObject(), "outputFluids")};
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
            timeModifier = JSONUtils.getAsFloat(json, "timeModifier", 1.0f);
            powerModifier = JSONUtils.getAsFloat(json, "powerModifier", 1.0f);
            radiation = JSONUtils.getAsFloat(json, "radiation", 1.0f);
            rarityModifier = JSONUtils.getAsFloat(json, "rarityModifier", 1.0f);
            temperature = JSONUtils.getAsFloat(json, "temperature", 1.0f);
            if (temperature > 1) {
                rarityModifier = temperature;
            }
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse params for recipe: "+recipeId);
        }
        return this.factory.create(recipeId, inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, radiation, rarityModifier);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull PacketBuffer buffer) {
        if(recipeId.getPath().contains("nc_ore_veins")) {
           // return (RECIPE) SERIALIZERS.get("nc_ore_veins").get().fromNetwork(recipeId, buffer);
        }
        try {

            int inputSize = buffer.readInt();
            ItemStackIngredient[] inputItems = new ItemStackIngredient[inputSize];
            for(int i = 0; i < inputSize; i++) {
                inputItems[i] = IngredientCreatorAccess.item().read(buffer);
            }

            int outputSize = buffer.readInt();
            ItemStack[] outputItems = new ItemStack[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputItems[i] =  buffer.readItem();
            }

            inputSize = buffer.readInt();
            FluidStackIngredient[] inputFluids = new FluidStackIngredient[inputSize];
            for(int i = 0; i < inputSize; i++) {
                inputFluids[i] = IngredientCreatorAccess.fluid().read(buffer);
            }

            outputSize = buffer.readInt();
            FluidStack[] outputFluids = new FluidStack[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputFluids[i] =  buffer.readFluidStack();
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
    public void toNetwork(@NotNull PacketBuffer buffer, @NotNull RECIPE recipe) {
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
                      ItemStackIngredient[] inputItems, ItemStack[] outputItems,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeMultiplier, double powerMultiplier, double radiationMultiplier, double rarityMultiplier);
    }
}