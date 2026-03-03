package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.debugLog;

public class TurbineRecipeSerializer<RECIPE extends NcRecipe> extends NcRecipeSerializer<RECIPE> {

    public TurbineRecipeSerializer(IFactory factory) {
        super(factory);
    }

    @Override
    protected @NotNull RECIPE fromJson(@NotNull JsonObject json) {

        FluidStackIngredient[] inputFluids = inputFluidsFromJson(json);
        FluidStackIngredient[] outputFluids = outputFluidsFromJson(json);

        double heatRequired = 1D;
        try {
            heatRequired = GsonHelper.getAsDouble(json, "heatRequired", 1D);
        } catch (Exception ex) {
            debugLog("Unable to parse params for recipe type: " + getCodeId());
        }
        return this.factory.create(getCodeId(), new ItemStackIngredient[]{}, new ItemStackIngredient[]{}, inputFluids, outputFluids, heatRequired, 1, 1, 1);
    }


    @Override
    protected RECIPE fromNetwork(@NotNull RegistryFriendlyByteBuf buffer) {
        try {
            ItemStackIngredient[] inputItems = readItems(buffer);
            ItemStackIngredient[] outputItems = readItems(buffer);
            FluidStackIngredient[] inputFluids = readFluids(buffer);
            FluidStackIngredient[] outputFluids = readFluids(buffer);

            double heatRequired = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();

            return this.factory.create(getCodeId(), new ItemStackIngredient[]{}, new ItemStackIngredient[]{}, inputFluids, outputFluids, heatRequired, 1, 1, 1);
        } catch (Exception e) {
            debugLog("Error reading from packet." + e);
            throw e;
        }
    }
}
