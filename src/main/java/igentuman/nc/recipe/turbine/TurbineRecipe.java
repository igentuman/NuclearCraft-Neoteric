package igentuman.nc.recipe.turbine;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public record TurbineRecipe(SizedFluidIngredient input, FluidOutput output, double powerModifier)
        implements MachineRecipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(ProcessorRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.fluidSize() < 1) return false;
        return input.test(in.getFluid(0));
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput in) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<TurbineRecipe> getSerializer() {
        return TurbineRecipes.TURBINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<TurbineRecipe> getType() {
        return TurbineRecipes.TURBINE_TYPE.get();
    }
}
