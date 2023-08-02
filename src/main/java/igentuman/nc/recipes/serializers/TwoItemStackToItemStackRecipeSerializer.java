package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.type.TwoItemStackToItemStackRecipe;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class TwoItemStackToItemStackRecipeSerializer<RECIPE extends TwoItemStackToItemStackRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public TwoItemStackToItemStackRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }
    @NotNull
    @Override
    public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        JsonElement input = GsonHelper.getAsJsonArray(json, JsonConstants.INPUT);
        ItemStackIngredient[] inputs = new ItemStackIngredient[input.getAsJsonArray().size()];
        int i = 0;
        for (JsonElement in : input.getAsJsonArray()) {
            inputs[i] = IngredientCreatorAccess.item().deserialize(in.getAsJsonObject());
            i++;
        }
        ItemStack[] outputItems;
        i = 0;
        if(GsonHelper.isArrayNode(json, JsonConstants.OUTPUT)) {
            JsonElement output = GsonHelper.getAsJsonArray(json, JsonConstants.OUTPUT);
            outputItems = new ItemStack[output.getAsJsonArray().size()];
            for (JsonElement out : output.getAsJsonArray()) {
                outputItems[i] = SerializerHelper.getItemStack(out.getAsJsonObject());
                i++;
            }
        } else {
           outputItems = new ItemStack[]{SerializerHelper.getItemStack(json, JsonConstants.OUTPUT)};
        }
        double timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
        double powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
        double radiation = GsonHelper.getAsDouble(json, "radiation", 1.0);
        if (outputItems.length == 0) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, inputs, outputItems, timeModifier, powerModifier, radiation);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            ItemStackIngredient inputIngredient2 = IngredientCreatorAccess.item().read(buffer);
            int outputSize = buffer.readInt();
            ItemStack[] outputList = new ItemStack[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputList[i] = buffer.readItem();
            }
            double timeModifier = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();
            return this.factory.create(recipeId, new ItemStackIngredient[]{inputIngredient, inputIngredient2}, outputList, timeModifier, powerModifier, radiation);
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
    public interface IFactory<RECIPE extends TwoItemStackToItemStackRecipe> {
        RECIPE create(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, double timeMultiplier, double powerMultiplier, double radiationMultiplier);
    }
}