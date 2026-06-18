package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NuclearBlastRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class NuclearBlastRecipeSerializer extends NcRecipeSerializer<NuclearBlastRecipe> {

    public NuclearBlastRecipeSerializer() {
        super((id, inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, radiation, rarityModifier) ->
                new NuclearBlastRecipe(id, inputItems, outputItems, rarityModifier));
    }

    @Override
    public @NotNull NuclearBlastRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ItemStackIngredient[] inputItems = inputItemsFromJson(json, recipeId);
        ItemStackIngredient[] outputItems = outputItemsFromJson(json, recipeId);

        double chance = GsonHelper.getAsDouble(json, "chance", 1.0);

        return new NuclearBlastRecipe(recipeId, inputItems, outputItems, chance);
    }

    @Override
    public NuclearBlastRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient[] inputItems = readItems(buffer);
        ItemStackIngredient[] outputItems = readItems(buffer);
        double chance = buffer.readDouble();
        return new NuclearBlastRecipe(recipeId, inputItems, outputItems, chance);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull NuclearBlastRecipe recipe) {
        buffer.writeInt(recipe.getInputItems().length);
        for (ItemStackIngredient input : recipe.getInputItems()) {
            input.write(buffer);
        }
        buffer.writeInt(recipe.getOutputItems().length);
        for (ItemStackIngredient output : recipe.getOutputItems()) {
            output.write(buffer);
        }
        buffer.writeDouble(recipe.chance);
    }
}
