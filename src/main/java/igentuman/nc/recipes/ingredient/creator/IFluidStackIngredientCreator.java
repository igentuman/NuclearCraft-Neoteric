package igentuman.nc.recipes.ingredient.creator;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

import static igentuman.nc.setup.registration.NCFluids.ALL_FLUID_ENTRIES;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    default FluidStackIngredient from(String name, int amount) {
        name = name.replace("/", "_");
        name = name.replace("-", "_");
        CompoundNBT tag = new CompoundNBT();
        tag.putString("FluidName", name);
        tag.putInt("Amount", amount);
        FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
        if(!stack.isEmpty()) {
            return from(stack.getFluid(), amount);
        }

        if(ALL_FLUID_ENTRIES.get(name) != null) {
            return from(ALL_FLUID_ENTRIES.get(name).getStill(), amount);
        }
        return null;
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }

    FluidStackIngredient from(ITag.INamedTag<Fluid> fluidIOptionalNamedTag, int amount);
}