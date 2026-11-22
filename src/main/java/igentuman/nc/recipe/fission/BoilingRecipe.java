package igentuman.nc.recipe.fission;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Boiling recipe: coolant fluid plus reactor heat converted to steam, spending {@code heatRequired} heat per batch. */
public record BoilingRecipe(SizedFluidIngredient input, FluidOutput output, int heatRequired)
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
    public RecipeSerializer<BoilingRecipe> getSerializer() {
        return FissionRecipes.BOILING_SERIALIZER.get();
    }

    @Override
    public RecipeType<BoilingRecipe> getType() {
        return FissionRecipes.BOILING_TYPE.get();
    }
}
