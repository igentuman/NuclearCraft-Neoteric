package igentuman.nc.recipe.heat_exchanger;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public record HeatExchangerRecipe(SizedFluidIngredient input, FluidOutput output, int heat)
        implements MachineRecipe<ProcessorRecipeInput> {

    public boolean isHot() {
        return heat > 0;
    }

    public boolean isCold() {
        return heat < 0;
    }

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
    public RecipeSerializer<HeatExchangerRecipe> getSerializer() {
        return HeatExchangerRecipes.HX_SERIALIZER.get();
    }

    @Override
    public RecipeType<HeatExchangerRecipe> getType() {
        return HeatExchangerRecipes.HX_TYPE.get();
    }
}
