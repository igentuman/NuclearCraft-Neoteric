package igentuman.nc.recipe.fusion;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Fusion coolant recipe: an input fluid converted to a hot output fluid, removing reactor heat at {@code coolingRate}. */
public record FusionCoolantRecipe(SizedFluidIngredient input, FluidOutput output, int coolingRate)
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
    public RecipeSerializer<FusionCoolantRecipe> getSerializer() {
        return FusionRecipes.COOLANT_SERIALIZER.get();
    }

    @Override
    public RecipeType<FusionCoolantRecipe> getType() {
        return FusionRecipes.COOLANT_TYPE.get();
    }
}
