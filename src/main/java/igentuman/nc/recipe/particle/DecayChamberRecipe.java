package igentuman.nc.recipe.particle;

import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
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
) implements Recipe<ParticleRecipeInput> {

    @Override
    public boolean matches(ParticleRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        return !in.particles().isEmpty() && particleInput.test(in.particles().get(0));
    }

    @Override
    public ItemStack assemble(ParticleRecipeInput in, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ParticleRecipes.DECAY_CHAMBER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ParticleRecipes.DECAY_CHAMBER_TYPE.get();
    }
}
