package igentuman.nc.particle;

import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record ParticleReactionPlan(
        Identifier recipeId,
        List<ParticleStack> particleInputs,
        List<ItemStack> itemInputs,
        List<FluidStack> fluidInputs,
        List<ParticleStack> particleOutputs,
        List<ItemStack> itemOutputs,
        List<FluidStack> fluidOutputs,
        long energyCost,
        double fractionalYield
) {
}
