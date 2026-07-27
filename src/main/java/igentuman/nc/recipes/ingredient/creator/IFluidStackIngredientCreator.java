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
        // TagUtil#getFluidByName resolves both bare names (defaulting to the minecraft
        // namespace) and full "modid:path" names via ResourceLocation, so it handles the
        // ":" case too. The previous ":" branch called back into this same method via
        // IngredientCreatorAccess.fluid().from(name, amount) with unchanged arguments,
        // recursing infinitely (StackOverflowError) for any foreign-mod fluid name not
        // already present in NC's own fluid registries.
        return IngredientCreatorAccess.fluid().from(new FluidStack(TagUtil.getFluidByName(name), amount));
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }
}