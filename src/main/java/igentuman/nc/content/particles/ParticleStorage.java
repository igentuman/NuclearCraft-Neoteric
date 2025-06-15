package igentuman.nc.content.particles;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The base particle storage class
 * 
 *
 */
public class ParticleStorage implements IParticleStorage, IParticleStackHandler
{

	protected ParticleStack particleStack;
	protected BlockEntity tile;
	protected long maxEnergy;
	protected long minEnergy;
	protected int capacity;

	
	public ParticleStorage(ParticleStack stack, long maxEnergy, int capacity, long minEnergy)
	{
		this.particleStack = stack;
		this.maxEnergy = maxEnergy;
		this.capacity = capacity;
		this.minEnergy = minEnergy;
	}
	
	public ParticleStorage(ParticleStack stack, long maxEnergy, int capacity)
	{
		this.particleStack = stack;
		this.maxEnergy = maxEnergy;
		this.capacity = capacity;
		this.minEnergy = 0;
	}
	
	public ParticleStorage(ParticleStack stack, long maxEnergy)
	{
		this.particleStack = stack;
		this.maxEnergy = maxEnergy;
		this.capacity = Integer.MAX_VALUE;
		this.minEnergy = 0;
	}
	
	public ParticleStorage()
	{
		this.particleStack = null;
		this.maxEnergy = Long.MAX_VALUE;
		this.capacity = Integer.MAX_VALUE;
		this.minEnergy = 0;
	}

	public void readFromNBT(CompoundTag nbt)
	{
		if (nbt.contains("particle_stack"))
		{
			this.particleStack = ParticleStack.loadParticleStackFromNBT(nbt.getCompound("particle_stack"));
		}
		else
		{
			this.particleStack = null;
		}
		
		this.maxEnergy = nbt.getLong("maxEnergy");
		this.capacity = nbt.getInt("capacity");
		this.minEnergy = nbt.getLong("minEnergy");
	}

	public CompoundTag writeToNBT(CompoundTag nbt)
	{
		CompoundTag tag = new CompoundTag();
		if (particleStack != null)
		{
			particleStack.writeToNBT(tag);
		}
		else
		{
			new ParticleStack().writeToNBT(tag);
		}
		nbt.put("particle_stack", tag);
		nbt.putLong("maxEnergy", maxEnergy);
		nbt.putInt("capacity", capacity);
		nbt.putLong("minEnergy", minEnergy);
		return nbt;
	}
	
	public CompoundTag writeToNBT(CompoundTag nbt, int id)
	{

		CompoundTag tag = new CompoundTag();
		writeToNBT(tag);
		nbt.put("ParticleStorage" + id, tag);
		
		return nbt;
	}

	public void readFromNBT(CompoundTag nbt, int id)
	{
		if (nbt.contains("ParticleStorage" + id))
		{
			CompoundTag tag = nbt.getCompound("ParticleStorage" + id);
			readFromNBT(tag);
		}
	}
	
	
	public void setParticleStack(ParticleStack stack)
	{
		this.particleStack = stack;

	}	
	
	public void setTileEntity(BlockEntity tile)
	{
		this.tile = tile;
	}

	public ParticleStorageInfo getInfo()
	{
		return new ParticleStorageInfo(this);
	}

	@Override
	public boolean reciveParticle(Direction side, ParticleStack stack)
	{
		if(stack != null)
		{
			if(particleStack == null)
			{
				particleStack = stack.copy();
				return true;
			}
			if(particleStack.getParticle() == stack.getParticle())
			{
				if(stack.getMeanEnergy() >= minEnergy && stack.getMeanEnergy() <= maxEnergy)
				{
					if(particleStack.getAmount()+ stack.getAmount() <= capacity)
					{
						
						particleStack.setMeanEnergy((stack.getMeanEnergy() * stack.getAmount() + particleStack.getMeanEnergy() * particleStack.getAmount())/ (stack.getAmount() + particleStack.getAmount()));
						particleStack.addAmount(stack.getAmount());
						return true;
					}
				}
			}
		}
		return false;
	}



	@Override
	public boolean canReciveParticle(Direction side, ParticleStack stack)
	{
		if(stack != null)
		{
			if(particleStack == null)
			{
				return true;
			}
			if(particleStack.getParticle() == stack.getParticle())
			{
				if(stack.getMeanEnergy() > minEnergy && stack.getMeanEnergy() < maxEnergy)
				{
					if(particleStack.getAmount()+ stack.getAmount() <= capacity)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canExtractParticle(Direction side)
	{
		return particleStack != null;
	}

	@Override
	public ParticleStack getParticleStack()
	{
		return particleStack;
	}

	@Override
	public long getMaxEnergy()
	{
		return maxEnergy;
	}

	@Override
	public long getMinEnergy()
	{
		return minEnergy;
	}
	
	@Override
	public int getCapacity()
	{
		return capacity;
	}
	
	public void setCapacity(int capacity)
	{
		this.capacity = capacity;
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
	public ParticleStack extractParticle(Direction side)
	{
		if (canExtractParticle(side))
		{
			ParticleStack stack = this.particleStack;
			this.particleStack = null;
			return stack;
		}
		return null;
	}
	
	@Override
	public ParticleStack extractParticle(Direction side, Particle type)
	{
		if (canExtractParticle(side))
		{
			if (particleStack.getParticle() == type)
			{
				return extractParticle(side);
			}
		}
		return null;
	}
	
	@Override
	public ParticleStack extractParticle(Direction side, int amount)
	{
		if(canExtractParticle(side))
		{
			ParticleStack stack = particleStack.copy();
			if(particleStack.getAmount() > amount)
			{
				stack.setAmount(amount);
			}
			else
			{
				stack.setAmount(particleStack.getAmount());
			}
			particleStack.removeAmount(amount);
		}
		return null;
	}
	
	@Override
	public ParticleStack extractParticle(Direction side, Particle type, int Amount)
	{

		if (canExtractParticle(side))
		{
			if (particleStack.getParticle() == type)
			{
				return extractParticle(side, Amount);
			}
		}
		return null;
	}

	@Override
	public ParticleStack getParticle()
	{
		if(particleStack != null)
		{
			return particleStack.copy();
		}
		return null;	
	}
}
