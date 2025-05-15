package igentuman.nc.content.particles;

import net.minecraft.core.Direction;

/**
 * source https://github.com/Lach01298/QMD
 */
public class ParticleStorageBeamline extends ParticleStorage
{

	private int length;
	public ParticleStorageBeamline(int length)
	{
		super(null,Integer.MAX_VALUE,Integer.MAX_VALUE);
		this.length = length;
	}
	
	public void setLength(int length)
	{
		this.length = length;	
	}
	
	public int getLength()
	{
		return length;	
	}
	
	public ParticleStack extractParticle(Direction side)
	{
		//todo QMDConfig.beamAttenuationRate = 0.02d
		if (canExtractParticle(side) && particleStack.getFocus() > length*0.02d)
		{	
			ParticleStack stack = this.particleStack;
			this.particleStack = null;
			stack.addFocus(-Equations.focusLoss(length, stack));
			return stack;
		}
		
		return null;
	}
	
	@Override
	public boolean reciveParticle(Direction side, ParticleStack stack)
	{
		if(stack != null)
		{
			if(stack.getFocus()>0)
			{
				particleStack = stack.copy();
				return true;
			}
		}
		return false;
	}

}
