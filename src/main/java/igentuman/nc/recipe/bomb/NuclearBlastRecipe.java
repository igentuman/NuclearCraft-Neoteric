package igentuman.nc.recipe.bomb;

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

public record NuclearBlastRecipe(Ingredient input, ItemOutput output, double chance)
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
        return NcBlastRecipes.SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return NcBlastRecipes.TYPE.get();
    }
}
