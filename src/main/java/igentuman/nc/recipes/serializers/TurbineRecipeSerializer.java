package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TurbineRecipeSerializer<RECIPE extends NcRecipe> extends NcRecipeSerializer<RECIPE> {

    public TurbineRecipeSerializer(IFactory factory) {
        super(factory);
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {

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

        FluidStack[] outputFluids = new FluidStack[0];
        try {
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
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse output fluid for recipe: "+recipeId);
        }
        double heatRequired = 1D;
        try {
            heatRequired = GsonHelper.getAsDouble(json, "heatRequired", 1D);
        } catch (Exception ex) {
            NuclearCraft.LOGGER.warn("Unable to parse params for recipe: "+recipeId);
        }
        return this.factory.create(recipeId, new ItemStackIngredient[]{}, new ItemStack[]{}, inputFluids, outputFluids, heatRequired, 1, 1, 1);
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
            double heatRequired = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();

            return this.factory.create(recipeId, new ItemStackIngredient[]{}, new ItemStack[]{}, inputFluids,  outputFluids, heatRequired, 1, 1, 1);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error reading from packet.", e);
            throw e;
        }
    }
}
