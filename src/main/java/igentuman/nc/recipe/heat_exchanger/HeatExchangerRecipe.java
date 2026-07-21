package igentuman.nc.recipe.heat_exchanger;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public record HeatExchangerRecipe(SizedFluidIngredient input, FluidOutput output, int heat)
        implements Recipe<ProcessorRecipeInput> {

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
    public ItemStack assemble(ProcessorRecipeInput in, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HeatExchangerRecipes.HX_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HeatExchangerRecipes.HX_TYPE.get();
    }
}
