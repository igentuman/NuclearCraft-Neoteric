package igentuman.nc.compat.jei.ingredient;

import igentuman.nc.content.particles.ParticleStack;
import mezz.jei.api.ingredients.IIngredientType;

/**
 * source https://github.com/Lach01298/QMD
 */
public final class ParticleType {
	public static final IIngredientType<ParticleStack> Particle = () -> ParticleStack.class;


	private ParticleType() 
	{

	}
}