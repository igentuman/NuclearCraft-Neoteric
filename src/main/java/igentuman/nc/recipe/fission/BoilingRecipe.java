package igentuman.nc.recipe.fission;

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

/**
 * Boiling recipe: coolant fluid + reactor heat -> steam fluid. {@code heatRequired} is the reactor
 * heat spent to convert one batch (the input/output amount). Consumed by the reactor steam logic.
 */
public record BoilingRecipe(SizedFluidIngredient input, FluidOutput output, int heatRequired)
        implements Recipe<ProcessorRecipeInput> {

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
        return FissionRecipes.BOILING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FissionRecipes.BOILING_TYPE.get();
    }
}
