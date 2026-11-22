package igentuman.nc.recipe.particle;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public record DecayChamberRecipe(
        ParticleIngredient particleInput,
        List<ParticleStack> particleOutputs,
        double crossSection,
        long releasedEnergyKeV,
        int priority
) implements MachineRecipe<ParticleRecipeInput> {

    @Override
    public boolean matches(ParticleRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        return !in.particles().isEmpty() && particleInput.test(in.particles().get(0));
    }

    @Override
    public ItemStack assemble(ParticleRecipeInput in) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<DecayChamberRecipe> getSerializer() {
        return ParticleRecipes.DECAY_CHAMBER_SERIALIZER.get();
    }

    @Override
    public RecipeType<DecayChamberRecipe> getType() {
        return ParticleRecipes.DECAY_CHAMBER_TYPE.get();
    }
}
