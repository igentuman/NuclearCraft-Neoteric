package igentuman.nc.recipe.kugelblitz;

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
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record KugelblitzRecipe(
        SizedIngredient input,
        ItemOutput output,
        double timeModifier,
        double powerModifier
) implements Recipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(ProcessorRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.size() < 1) return false;
        return input.test(in.getItem(0));
    }

    public int getBaseTime() {
        return (int) (timeModifier * 50);
    }

    public int getEnergy() {
        return (int) (powerModifier * 1000);
    }

    public ItemStack getInputStack() {
        ItemStack[] items = input.ingredient().getItems();
        return items.length > 0 ? items[0].copyWithCount(input.count()) : ItemStack.EMPTY;
    }

    public ItemStack getResultStack() {
        return output.resolve();
    }

    public boolean isComplete() {
        return !input.ingredient().isEmpty() && output.isComplete();
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
        list.add(input.ingredient());
        return list;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return KugelblitzRecipes.KUGELBLITZ_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return KugelblitzRecipes.KUGELBLITZ_TYPE.get();
    }
}
