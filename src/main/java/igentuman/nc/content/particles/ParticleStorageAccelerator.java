package igentuman.nc.content.particles;

import net.minecraft.core.Direction;

/**
 * source https://github.com/Lach01298/QMD
 */
public class ParticleStorageAccelerator extends ParticleStorage
{
	public ParticleStorageAccelerator()
	{
		super(null,Long.MAX_VALUE,Integer.MAX_VALUE);
	
	}
	
	public void setMaxEnergy(long maxEnergy)
	{
		this.maxEnergy = maxEnergy;
	}
	
	public void setMinEnergy(long minEnergy)
	{
		this.minEnergy = minEnergy;
	}
	
	
	@Override
	public boolean reciveParticle(Direction side, ParticleStack stack)
	{
		if(stack != null)
		{
			if(stack.getMeanEnergy() >= minEnergy && stack.getMeanEnergy() <= maxEnergy)
			{
				this.particleStack = stack;
				return true;
			}
			return false;
		}
		this.particleStack = stack;
		return true;
	}
	
	
	
	
	
	
	
}
