package igentuman.nc.content.particles.creator;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleIngredient;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.annotation.NothingNullByDefault;

import java.util.Objects;

/**
 * Interface for creating particle ingredients.
 */
@NothingNullByDefault
public interface IParticleIngredientCreator extends IIngredientCreator<Particle, ParticleStack, ParticleIngredient> {

    @Override
    default ParticleIngredient from(ParticleStack instance) {
        Objects.requireNonNull(instance, "ParticleIngredients cannot be created from a null ParticleStack.");
        return from(instance, instance.getAmount());
    }

    /**
     * Creates a Particle Ingredient that matches a given particle stack with a specified amount.
     *
     * @param stack  Particle stack to match.
     * @param amount Amount needed.
     */
    default ParticleIngredient from(ParticleStack stack, int amount) {
        Objects.requireNonNull(stack, "ParticleIngredients cannot be created from a null ParticleStack.");
        if (stack.getParticle() == null) {
            throw new IllegalArgumentException("ParticleIngredients cannot be created using a stack with no particle.");
        }
        // Copy the stack to ensure it doesn't get modified afterwards
        stack = stack.copy();
        return from(stack.getParticle(), amount, stack.getMeanEnergy(), stack.getFocus());
    }

    /**
     * Creates a Particle Ingredient that matches a provided particle.
     *
     * @param particle Particle to match.
     */
    default ParticleIngredient from(Particle particle) {
        return from(particle, 1, 0, 0);
    }

    /**
     * Creates a Particle Ingredient that matches a provided particle and amount.
     *
     * @param particle Particle to match.
     * @param amount   Amount needed.
     */
    default ParticleIngredient from(Particle particle, int amount) {
        return from(particle, amount, 0, 0);
    }

    /**
     * Creates a Particle Ingredient that matches a provided particle, amount, and energy.
     *
     * @param particle   Particle to match.
     * @param amount     Amount needed.
     * @param meanEnergy Mean energy of the particle.
     */
    default ParticleIngredient from(Particle particle, int amount, long meanEnergy) {
        return from(particle, amount, meanEnergy, 0);
    }

    /**
     * Creates a Particle Ingredient that matches a provided particle, amount, energy, and focus.
     *
     * @param particle   Particle to match.
     * @param amount     Amount needed.
     * @param meanEnergy Mean energy of the particle.
     * @param focus      Focus of the particle beam.
     */
    ParticleIngredient from(Particle particle, int amount, long meanEnergy, double focus);
}