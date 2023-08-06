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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NcRecipeSerializer<RECIPE extends NcRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public NcRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Override
    public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ItemStackIngredient[] inputItems = new ItemStackIngredient[0];
        if(json.has(JsonConstants.INPUT)) {
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

        ItemStack[] outputItems = new ItemStack[0];
        if(json.has(JsonConstants.OUTPUT)) {
            if (GsonHelper.isArrayNode(json, JsonConstants.OUTPUT)) {
                JsonElement output = GsonHelper.getAsJsonArray(json, JsonConstants.OUTPUT);
                outputItems = new ItemStack[output.getAsJsonArray().size()];
                int i = 0;
                for (JsonElement out : output.getAsJsonArray()) {
                    outputItems[i] = SerializerHelper.getItemStack(out.getAsJsonObject());
                    i++;
                }
            } else {
                JsonElement output = GsonHelper.getAsJsonObject(json, JsonConstants.OUTPUT);
                outputItems = new ItemStack[]{SerializerHelper.getItemStack(output.getAsJsonObject())};
            }
        }

        FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
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

        FluidStack[] outputFluids = new FluidStack[0];
        if(json.has("outputFluids")) {
            if (GsonHelper.isArrayNode(json, "outputFluids")) {
                JsonElement output = GsonHelper.getAsJsonArray(json, "outputFluids");
                outputFluids = new FluidStack[output.getAsJsonArray().size()];
                int i = 0;
                for (JsonElement out : output.getAsJsonArray()) {
                    outputFluids[i] = SerializerHelper.getFluidStack(out.getAsJsonObject());
                    i++;
                }
            } else {
                JsonElement output = GsonHelper.getAsJsonObject(json, "outputFluids");
                outputFluids = new FluidStack[]{SerializerHelper.getFluidStack(output.getAsJsonObject(), "outputFluids")};
            }
        }
        
        double timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
        double powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
        double radiation = GsonHelper.getAsDouble(json, "radiation", 1.0);

        return this.factory.create(recipeId, inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, radiation);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
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

            return this.factory.create(recipeId, inputItems, outputItems, inputFluids,  outputFluids, timeModifier, powerModifier, radiation);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error reading itemstack to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error writing itemstack to itemstack recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends NcRecipe> {
        RECIPE create(ResourceLocation id,
                      ItemStackIngredient[] inputItems, ItemStack[] outputItems,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeMultiplier, double powerMultiplier, double radiationMultiplier);
    }
}