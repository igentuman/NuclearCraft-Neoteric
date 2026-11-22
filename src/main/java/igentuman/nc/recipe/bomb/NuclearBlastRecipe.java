package igentuman.nc.recipe.bomb;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record NuclearBlastRecipe(Ingredient input, ItemOutput output, double chance)
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
    public RecipeSerializer<NuclearBlastRecipe> getSerializer() {
        return NcBlastRecipes.SERIALIZER.get();
    }

    @Override
    public RecipeType<NuclearBlastRecipe> getType() {
        return NcBlastRecipes.TYPE.get();
    }
}
