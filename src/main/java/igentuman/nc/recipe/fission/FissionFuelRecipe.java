package igentuman.nc.recipe.fission;

import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Fission fuel processing recipe: one fuel item burned over {@code processTime} ticks, producing
 * {@code power} FE/t and {@code heat} heat/t, yielding a depleted item. Consumed by the reactor
 * runtime logic, not by the universal {@code RecipeInfo} loop.
 */
public record FissionFuelRecipe(Ingredient input, ItemOutput output, int processTime, int power, int heat)
        implements Recipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(ProcessorRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.size() < 1) return false;
        return input.test(in.getItem(0));
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput in, HolderLookup.Provider registries) {
        return output.resolve();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.resolve();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FissionRecipes.FUEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FissionRecipes.FUEL_TYPE.get();
    }
}
