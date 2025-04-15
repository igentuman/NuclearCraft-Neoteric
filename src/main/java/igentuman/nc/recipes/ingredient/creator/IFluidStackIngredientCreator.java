package igentuman.nc.recipes.ingredient.creator;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.TagUtil;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    default FluidStackIngredient from(String name, int amount) {
        if(NCFluids.NC_MATERIALS.get(name) != null) {
            return from(NCFluids.NC_MATERIALS.get(name).getStill(), amount);
        }
        if (NCFluids.ALL_FLUID_ENTRIES.get(name) != null) {
            return from(NCFluids.ALL_FLUID_ENTRIES.get(name).getStill(), amount);
        }
        if (!name.contains(":")) {
            return IngredientCreatorAccess.fluid().from(new FluidStack(TagUtil.getFluidByName(name), amount));
        }
        return IngredientCreatorAccess.fluid().from(name, amount);
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }
}