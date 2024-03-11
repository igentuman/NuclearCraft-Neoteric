package igentuman.nc.recipes.ingredient.creator;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    default FluidStackIngredient from(String name, int amount) {
        if(NCFluids.NC_MATERIALS.get(name) != null) {
          //  return from(NCFluids.NC_MATERIALS.get(name).getStill(), amount);
        }
       // return from(NCFluids.NC_GASES.get(name).getStill(), amount);
        return null;
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }

    FluidStackIngredient from(Tags.IOptionalNamedTag<Fluid> fluidIOptionalNamedTag, int amount);
}