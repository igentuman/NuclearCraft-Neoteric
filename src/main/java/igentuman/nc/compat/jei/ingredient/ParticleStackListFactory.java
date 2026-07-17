package igentuman.nc.compat.jei.ingredient;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * source https://github.com/Lach01298/QMD
 */
public final class ParticleStackListFactory
{
	private ParticleStackListFactory()
	{

	}

	public static List<ParticleStack> create()
	{
		List<ParticleStack> particleStacks = new ArrayList<>();

		Map<String, Particle> registeredParticles = Particles.particles;
		for (Particle particle : registeredParticles.values())
		{
			ParticleStack particleStack = new ParticleStack(particle, 0, 0, 0);
			particleStacks.add(particleStack);
		}

		return particleStacks;
	}
}
