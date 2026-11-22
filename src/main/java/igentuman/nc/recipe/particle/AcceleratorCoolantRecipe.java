package igentuman.nc.recipe.particle;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

public record AcceleratorCoolantRecipe(
        SizedFluidIngredient input,
        FluidOutput output,
        long heatPerMb,
        @Nullable Long minimumTemperatureK
) implements MachineRecipe<ProcessorRecipeInput> {

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
    public RecipeSerializer<AcceleratorCoolantRecipe> getSerializer() {
        return ParticleRecipes.ACCELERATOR_COOLANT_SERIALIZER.get();
    }

    @Override
    public RecipeType<AcceleratorCoolantRecipe> getType() {
        return ParticleRecipes.ACCELERATOR_COOLANT_TYPE.get();
    }
}
