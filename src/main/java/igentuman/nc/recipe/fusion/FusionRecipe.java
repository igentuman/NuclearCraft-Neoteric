package igentuman.nc.recipe.fusion;

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

import java.util.List;

/** Fusion recipe: two input fluids fused at an optimal temperature over a process time, yielding output fluids and energy. */
public record FusionRecipe(
        SizedFluidIngredient inputA,
        SizedFluidIngredient inputB,
        List<FluidOutput> outputs,
        int energy,
        double optimalTemperature,
        int processTime
) implements Recipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(ProcessorRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.fluidSize() < 2) return false;
        return inputA.test(in.getFluid(0)) && inputB.test(in.getFluid(1));
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
        return FusionRecipes.FUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FusionRecipes.FUSION_TYPE.get();
    }
}
