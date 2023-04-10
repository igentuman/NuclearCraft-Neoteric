package igentuman.nc.recipes.cache.type;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.HashedFluid;
import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;


public class FluidInputCache<RECIPE extends NcRecipe> extends NBTSensitiveInputCache<Fluid, HashedFluid, FluidStack, FluidStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, FluidStackIngredient inputIngredient) {
        if (inputIngredient instanceof FluidStackIngredientCreator.SingleFluidStackIngredient single) {
            HashedFluid input = HashedFluid.create(single.getInputRaw());
            addNbtInputCache(input, recipe);
        } else if (inputIngredient instanceof FluidStackIngredientCreator.TaggedFluidStackIngredient tagged) {
            for (Fluid input : tagged.getRawInput()) {
                addInputCache(input, recipe);
            }
        } else if (inputIngredient instanceof FluidStackIngredientCreator.MultiFluidStackIngredient multi) {
            return mapMultiInputs(recipe, multi);
        } else {
            //This should never really happen as we don't really allow for custom ingredients especially for networking,
            // but if it does add it as a fallback
            return true;
        }
        return false;
    }

    @Override
    protected Fluid createKey(FluidStack stack) {
        return stack.getFluid();
    }

    @Override
    protected HashedFluid createNbtKey(FluidStack stack) {
        return HashedFluid.raw(stack);
    }

    @Override
    public boolean isEmpty(FluidStack input) {
        return input.isEmpty();
    }
}