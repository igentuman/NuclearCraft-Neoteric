package igentuman.nc.recipe.particle;

import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record ParticleRecipeInput(
        List<ItemStack> items,
        List<FluidStack> fluids,
        List<ParticleStack> particles
) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
