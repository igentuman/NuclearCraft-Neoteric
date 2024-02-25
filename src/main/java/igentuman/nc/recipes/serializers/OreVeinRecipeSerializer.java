package igentuman.nc.recipes.serializers;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class OreVeinRecipeSerializer<RECIPE extends NcRecipe> extends NcRecipeSerializer<RECIPE> {

    public OreVeinRecipeSerializer(IFactory factory) {
        super(factory);
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
            ItemStackIngredient[] outputItems = new ItemStackIngredient[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputItems[i] =  IngredientCreatorAccess.item().read(buffer);
            }

            inputSize = buffer.readInt();
            FluidStackIngredient[] inputFluids = new FluidStackIngredient[inputSize];
            for(int i = 0; i < inputSize; i++) {
                inputFluids[i] = IngredientCreatorAccess.fluid().read(buffer);
            }

            outputSize = buffer.readInt();
            FluidStackIngredient[] outputFluids = new FluidStackIngredient[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputFluids[i] =  IngredientCreatorAccess.fluid().read(buffer);
            }

            double timeModifier = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();
            double rarity = buffer.readDouble();

            return this.factory.create(recipeId, inputItems, outputItems, inputFluids,  outputFluids, timeModifier, powerModifier, radiation, rarity);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error reading from packet.", e);
            throw e;
        }
    }
}
