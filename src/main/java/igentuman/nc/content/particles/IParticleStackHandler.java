package igentuman.nc.content.particles;

import net.minecraft.core.Direction;

/**
 * source https://github.com/Lach01298/QMD
 */
public interface IParticleStackHandler
{

	/**
	 *
	 * @param side
	 * @param stack - the ParticleStack to be inputed
	 * @return if the stack could be inputed
	 */
	boolean reciveParticle(Direction side, ParticleStack stack);
	
	
	/**
	 *
	 * @param side
	 * @return the extracted ParticleStack
	 */
	ParticleStack extractParticle(Direction side);
	
	/**
	 *
	 * @param side
	 * @param type the type of particle
	 * @return the extracted ParticleStack
	 */
	ParticleStack extractParticle(Direction side, Particle type);
	
	/**
	 *
	 * @param side
	 *@param Amount the amount of particles
	 * @return the extracted ParticleStack
	 */
	ParticleStack extractParticle(Direction side, int Amount);
	
	/**
	 *
	 * @param side
	 * @param type the type of particle
	 * @param Amount the amount of particles
	 * @return the extracted ParticleStack
	 */
	ParticleStack extractParticle(Direction side, Particle type, int Amount);
	
	/**
	 *
	 *
	 * @return a copy of the ParticleStack
	 */
	ParticleStack getParticle();
	
	

	/**
	 *
	 * @param side
	 * @param stack - the ParticleStack to be inputed
	 * @return if the stack could be inputed
	 */
	boolean canReciveParticle(Direction side, ParticleStack stack);
	
	/**
	 *
	 * @param side
	 * @return if the a ParticleStack could be extracted
	 */
	boolean canExtractParticle(Direction side);
	
	
	
}
