package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.debugLog;

public class HeatExchangerRecipeSerializer<RECIPE extends NcRecipe> extends NcRecipeSerializer<RECIPE> {

    public HeatExchangerRecipeSerializer(IFactory<RECIPE> factory) {
        super(factory);
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ItemStackIngredient[] inputItems = inputItemsFromJson(json, recipeId);
        ItemStackIngredient[] outputItems = outputItemsFromJson(json, recipeId);
        FluidStackIngredient[] inputFluids = inputFluidsFromJson(json, recipeId);
        FluidStackIngredient[] outputFluids = outputFluidsFromJson(json, recipeId);

        double timeModifier = 1D;
        double powerModifier = 1D;
        double heat = 0D;
        try {
            timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
            powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
            heat = GsonHelper.getAsDouble(json, "heat", 0.0);
        } catch (Exception ex) {
            debugLog("Unable to parse params for recipe: " + recipeId);
        }
        // Signed heat rides the radiation slot; NcRecipe.write()/fromNetwork already round-trips it.
        return this.factory.create(recipeId, inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, heat, 1D);
    }
}
