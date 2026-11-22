package igentuman.nc.recipe.fission;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Fission fuel recipe: a fuel item burned over a process time, producing power and heat and yielding a depleted item. */
public record FissionFuelRecipe(Ingredient input, ItemOutput output, int processTime, int power, int heat)
        implements MachineRecipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(ProcessorRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.size() < 1) return false;
        return input.test(in.getItem(0));
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput in) {
        return output.resolve();
    }

    @Override
    public RecipeSerializer<FissionFuelRecipe> getSerializer() {
        return FissionRecipes.FUEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<FissionFuelRecipe> getType() {
        return FissionRecipes.FUEL_TYPE.get();
    }
}
