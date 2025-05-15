package igentuman.nc.content.particles;

import javax.annotation.Nullable;

/**
 * source https://github.com/Lach01298/QMD
 */
public interface IParticleStorage
{
	  /**
     * @return ParticleStack representing the particles in the tank
     */
	 @Nullable
    ParticleStack getParticleStack();

    /**
     * @return The Maximum energy of a particle that the tank can hold.
     */
    long getMaxEnergy();

    /**
     * @return The Minimum energy of a particle that the tank can hold.
     */
    long getMinEnergy();

    /**
     * @return The Maximum number of particles that the tank can hold.
     */
    int getCapacity();
    
    /**
     * @return State information for the IParticleStorage.
     */
    ParticleStorageInfo getInfo();

}
