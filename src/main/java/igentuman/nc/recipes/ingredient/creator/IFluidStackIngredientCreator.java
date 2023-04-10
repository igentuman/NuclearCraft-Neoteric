package igentuman.nc.recipes.ingredient.creator;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    default FluidStackIngredient from(String name, int amount) {

        return from(NCFluids.NC_MATERIALS.get(name).getStill(), amount);
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }
}