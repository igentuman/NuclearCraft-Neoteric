package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ItemStackToItemStackRecipeSerializer<RECIPE extends ItemStackToItemStackRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ItemStackToItemStackRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                            GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
        ItemStack output = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        Double timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
        Double powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
        Double radiation = GsonHelper.getAsDouble(json, "radiation", 1.0);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, inputIngredient, output, timeModifier, powerModifier, radiation);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            ItemStack output = buffer.readItem();
            Double timeModifier = buffer.readDouble();
            Double powerModifier = buffer.readDouble();
            Double radiation = buffer.readDouble();
            return this.factory.create(recipeId, inputIngredient, output, timeModifier, powerModifier, radiation);
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
    public interface IFactory<RECIPE extends ItemStackToItemStackRecipe> {
        RECIPE create(ResourceLocation id, ItemStackIngredient input, ItemStack output, double timeMultiplier, double powerMultiplier, double radiationMultiplier);
    }
}