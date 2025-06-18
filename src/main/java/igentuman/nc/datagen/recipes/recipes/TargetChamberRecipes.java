package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.content.particles.Particles.neutron;
import static igentuman.nc.content.particles.Particles.positron;

public class TargetChamberRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        TargetChamberRecipes.consumer = consumer;
        ID = "target_chamber";
        targetChamberItems(ingotIngredient("uranium"), particles(2000000, 0.1d, 100000, positron), List.of(particles(1000000, 0.05d, 50000, neutron)), ingotIngredient("thorium"), 50000000, 10);
    }

    private static ParticleStack particles(int energy, double focus, int amount, Particle particles) {
        return new ParticleStack(particles, amount, energy, focus);
    }

    private static void targetChamberItems(NcIngredient inputItems, ParticleStack inputParticle, List<ParticleStack> outputParticles, NcIngredient outputItems, long maxEnergy, double crossSection) {
        targetChamber(List.of(), List.of(inputItems), List.of(inputParticle), outputParticles, List.of(), List.of(outputItems), maxEnergy, crossSection);
    }

    private static void targetChamberFluids(List<FluidStackIngredient> inputFluids, ParticleStack inputParticle, List<ParticleStack> outputParticles, List<FluidStackIngredient> outputFluids, long maxEnergy, double crossSection) {
        targetChamber(inputFluids, List.of(), List.of(inputParticle), outputParticles, outputFluids, List.of(), maxEnergy, crossSection);
    }
}
