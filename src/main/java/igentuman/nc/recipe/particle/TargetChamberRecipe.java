package igentuman.nc.recipe.particle;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public record TargetChamberRecipe(
        List<SizedIngredient> itemInputs,
        List<SizedFluidIngredient> fluidInputs,
        ParticleIngredient particleInput,
        List<ItemOutput> itemOutputs,
        List<FluidOutput> fluidOutputs,
        List<ParticleStack> particleOutputs,
        double crossSection,
        long releasedEnergyKeV,
        int priority
) implements MachineRecipe<ParticleRecipeInput> {

    @Override
    public boolean matches(ParticleRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.particles().isEmpty() || !particleInput.test(in.particles().get(0))) return false;
        boolean itemOk = itemInputs.isEmpty()
                || (!in.items().isEmpty() && itemInputs.stream().anyMatch(i -> i.test(in.items().get(0))));
        boolean fluidOk = fluidInputs.isEmpty()
                || (!in.fluids().isEmpty() && fluidInputs.stream().anyMatch(i -> i.test(in.fluids().get(0))));
        return itemOk && fluidOk;
    }

    @Override
    public ItemStack assemble(ParticleRecipeInput in) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<TargetChamberRecipe> getSerializer() {
        return ParticleRecipes.TARGET_CHAMBER_SERIALIZER.get();
    }

    @Override
    public RecipeType<TargetChamberRecipe> getType() {
        return ParticleRecipes.TARGET_CHAMBER_TYPE.get();
    }
}
