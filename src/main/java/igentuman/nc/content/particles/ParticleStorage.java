package igentuman.nc.content.particles;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * The base particle storage class
 * 
 *
 */
public class ParticleStorage implements IParticleStorage, IParticleStackHandler
{

	protected ParticleStack particleStack;
	public List<ParticleStack> outputParticles = new ArrayList<>();
	protected ParticleStack clientParticle;
	protected BlockEntity tile;
	protected long maxEnergy;
	protected long minEnergy;
	protected int capacity;
	protected Direction sourceDir = Direction.UP;
	
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
			this.clientParticle = ParticleStack.loadParticleStackFromNBT(nbt.getCompound("particle_stack"));
		}
		else
		{
			this.clientParticle = null;
		}
		
		this.maxEnergy = nbt.getLong("maxEnergy");
		this.capacity = nbt.getInt("capacity");
		this.minEnergy = nbt.getLong("minEnergy");
		this.sourceDir = Direction.byName(nbt.getString("sourceDir"));
		int outputCount = nbt.getInt("outputParticlesCount");
		this.outputParticles = new java.util.ArrayList<>(outputCount);
		if (nbt.contains("outputParticles"))
		{
			CompoundTag outputs = nbt.getCompound("outputParticles");
			for(int i = 0; i < outputCount; i++)
			{
				if(outputs.contains("particle_"+i))
				{
					ParticleStack stack = ParticleStack.loadParticleStackFromNBT(outputs.getCompound("particle_"+i));
					if(stack != null)
					{
						outputParticles.add(stack);
					}
				}
			}
		}
	}

	public CompoundTag writeToNBT(CompoundTag nbt)
	{
		CompoundTag tag = new CompoundTag();
		if (clientParticle != null)
		{
			clientParticle.writeToNBT(tag);
		} else {
			new ParticleStack().writeToNBT(tag);
		}
		nbt.put("particle_stack", tag);
		nbt.putLong("maxEnergy", maxEnergy);
		nbt.putInt("capacity", capacity);
		nbt.putLong("minEnergy", minEnergy);
		nbt.putInt("outputParticlesCount", outputParticles.size());
		CompoundTag outputs = new CompoundTag();
		int id = 0;
		for(ParticleStack stack : outputParticles) {
			outputs.put("particle_"+id, stack.writeToNBT(outputs));
		}
		nbt.put("outputParticles", outputs);
		if(sourceDir == null)
		{
			sourceDir = Direction.UP;
		}
		nbt.putString("sourceDir", sourceDir.name());
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
				tile.setChanged();
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
						sourceDir = side != null ? side : Direction.UP;
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
		if ((sourceDir == null || !sourceDir.equals(side)) && canExtractParticle(side))
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
		if ((sourceDir == null || !sourceDir.equals(side)) && canExtractParticle(side))
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
		if((sourceDir == null || !sourceDir.equals(side)) && canExtractParticle(side))
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

		if (!sourceDir.equals(side) && canExtractParticle(side))
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

    public String getCacheKey() {
		return particleStack == null || particleStack.getParticle() == null ? "" : particleStack.getParticle().getName();
    }

	public void clearClient() {
		clientParticle = null;
	}

	public void clearServer() {
		clientParticle = particleStack;
		particleStack = null;
		outputParticles.clear();
	}

	public void clear() {
		particleStack = null;
		outputParticles.clear();
	}

	public ParticleStack getClientParticleStack() {
		return clientParticle;
	}
}
