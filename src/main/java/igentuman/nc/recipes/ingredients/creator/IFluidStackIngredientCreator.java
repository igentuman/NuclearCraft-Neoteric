package igentuman.nc.recipes.ingredients.creator;

import igentuman.nc.recipes.ingredients.FluidStackIngredient;
import igentuman.nc.util.provider.IFluidProvider;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    default FluidStackIngredient from(IFluidProvider provider, int amount) {
        Objects.requireNonNull(provider, "FluidStackIngredients cannot be created from a null fluid provider.");
        return from(provider.getFluidStack(amount));
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }
}