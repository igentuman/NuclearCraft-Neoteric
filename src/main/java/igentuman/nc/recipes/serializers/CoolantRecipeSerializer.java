package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.debugLog;

public class CoolantRecipeSerializer<RECIPE extends NcRecipe> extends NcRecipeSerializer<RECIPE> {

    public CoolantRecipeSerializer(IFactory factory) {
        super(factory);
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {

        FluidStackIngredient[] inputFluids = inputFluidsFromJson(json, recipeId);
        FluidStackIngredient[] outputFluids = outputFluidsFromJson(json, recipeId);

        double coolingRate = 1000D;
        try {
            coolingRate = GsonHelper.getAsDouble(json, "coolingRate", 1000.0);
        } catch (Exception ex) {
            debugLog("Unable to parse params for recipe: "+recipeId);
        }
        return this.factory.create(recipeId, new ItemStackIngredient[0], new ItemStackIngredient[0], inputFluids, outputFluids, 1, 1, 1, coolingRate);
    }


    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient[] inputItems = readItems(buffer);
            ItemStackIngredient[] outputItems = readItems(buffer);
            FluidStackIngredient[] inputFluids = readFluids(buffer);
            FluidStackIngredient[] outputFluids = readFluids(buffer);

            double coolingRate = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();

            return this.factory.create(recipeId, new ItemStackIngredient[]{}, new ItemStackIngredient[]{}, inputFluids,  outputFluids, coolingRate, 1, 1, 1);
        } catch (Exception e) {
            debugLog("Error reading from packet." + e);
            throw e;
        }
    }
}
