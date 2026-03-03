package igentuman.nc.recipes.serializers;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.debugLog;

public class FusionRecipeSerializer<RECIPE extends NcRecipe> extends NcRecipeSerializer<RECIPE> {

    public FusionRecipeSerializer(IFactory factory) {
        super(factory);
    }

    @Override
    protected RECIPE fromNetwork(@NotNull RegistryFriendlyByteBuf buffer) {
        try {
            ItemStackIngredient[] inputItems = readItems(buffer);
            ItemStackIngredient[] outputItems = readItems(buffer);
            FluidStackIngredient[] inputFluids = readFluids(buffer);
            FluidStackIngredient[] outputFluids = readFluids(buffer);

            double timeModifier = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();
            double temperature = buffer.readDouble();

            return this.factory.create(getCodeId(), inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, radiation, temperature);
        } catch (Exception e) {
            debugLog("Error reading from packet." + e);
            throw e;
        }
    }
}
