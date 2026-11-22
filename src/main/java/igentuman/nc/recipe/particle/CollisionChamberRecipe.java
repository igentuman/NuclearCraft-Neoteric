package igentuman.nc.recipe.particle;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public record CollisionChamberRecipe(
        List<ParticleIngredient> particleInputs,
        List<ParticleStack> particleOutputs,
        double crossSection,
        long releasedEnergyKeV,
        boolean orderedInputs,
        int priority
) implements MachineRecipe<ParticleRecipeInput> {

    public CollisionChamberRecipe {
        if (particleInputs.size() != 2) {
            throw new IllegalArgumentException("Collision chamber recipe requires exactly two particle inputs");
        }
    }

    public boolean matchesPair(ParticleStack a, ParticleStack b) {
        if (particleInputs.get(0).test(a) && particleInputs.get(1).test(b)) return true;
        return !orderedInputs && particleInputs.get(0).test(b) && particleInputs.get(1).test(a);
    }

    @Override
    public boolean matches(ParticleRecipeInput in, Level level) {
        if (level.isClientSide() || in.particles().size() < 2) return false;
        return matchesPair(in.particles().get(0), in.particles().get(1));
    }

    @Override
    public ItemStack assemble(ParticleRecipeInput in) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<CollisionChamberRecipe> getSerializer() {
        return ParticleRecipes.COLLISION_CHAMBER_SERIALIZER.get();
    }

    @Override
    public RecipeType<CollisionChamberRecipe> getType() {
        return ParticleRecipes.COLLISION_CHAMBER_TYPE.get();
    }
}
