package igentuman.nc.content.particles;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.content.particles.ParticleSources.moleAmount;
import static igentuman.nc.content.particles.ParticleSources.sources;
import static igentuman.nc.content.particles.Particles.particles;

public interface IItemParticleAmount
{

	/** gets the items hardcoded capacity */
	int getItemCapacity(ItemStack stack);

	/** gets the items capacity by reading its nbt data*/
	static int getCapacity(ItemStack stack)
	{
		CompoundTag nbt = getStorageNBT(stack);
		if (!nbt.contains("particle_capacity")) {
			return moleAmount;
		}
		
		return nbt.getInt("particle_capacity");
	}

	default Particle getParticle(ItemStack stack)
	{
		CompoundTag nbt = getStorageNBT(stack);
		if (!nbt.contains("particle")) {
			nbt.putString("particle", sources.get(stack.getItem().toString()).getParticle().name);
		}
		return particles.get(nbt.getString("particle"));
	}

	default int getAmountStored(ItemStack stack)
	{
		CompoundTag nbt = getStorageNBT(stack);
		if (!nbt.contains("particle_amount")) {
			return 0;
		}
		return nbt.getInt("particle_amount");
	}

	default void setAmountStored(ItemStack stack, int amount)
	{
		if(stack.getItem() instanceof IItemParticleAmount)
		{
			CompoundTag nbt = getStorageNBT(stack);
            nbt.putInt("particle_amount", Math.max(amount, getCapacity(stack)));
		}
	}
	
	
	default ItemStack fill(ItemStack stack, int amount, String type)
	{
		if(getAmountStored(stack) + amount <= getCapacity(stack))
		{
			setAmountStored(stack,getAmountStored(stack)+amount);
		}
		
		return stack;
	}
	
	default ItemStack use(ItemStack stack, int amount)
	{
		if(getAmountStored(stack) > amount)
		{
			setAmountStored(stack,getAmountStored(stack)-amount);
		}
		else if (getAmountStored(stack) == amount)
		{
			return getEmptyItem();
		}
		
		return stack;
	}
	
	default ItemStack getEmptyItem()
	{
		return  ItemStack.EMPTY;
	}
	
	default boolean isEmptyItem(ItemStack stack)
	{
		return  stack == getEmptyItem() || getAmountStored(stack) <= 0;
	}
	
	static ItemStack cleanNBT(ItemStack stack)
	{
		ItemStack newStack = stack.copy();
		newStack.setTag(null);

		return newStack;
	}
	
	
	static ItemStack fullItem(ItemStack stack)
	{
		if(stack.getItem() instanceof IItemParticleAmount)
		{
			setStorageNBT(stack);
			IItemParticleAmount item = (IItemParticleAmount) stack.getItem();
			item.setAmountStored(stack, item.getItemCapacity(stack));
		}
		return stack;
	}

	static CompoundTag getStorageNBT(ItemStack stack)
	{
		CompoundTag nbt = stack.getOrCreateTag();
		if (!nbt.contains("particle_storage"))
		{
			CompoundTag storage = new CompoundTag();
			storage.putInt("particle_amount", ParticleSources.getCapacity(stack));
			storage.putInt("particle_capacity", ParticleSources.getCapacity(stack));
			nbt.put("particle_storage", storage);
		}
		return nbt.getCompound("particle_storage");
	}

	static void setStorageNBT(ItemStack stack)
	{
		if (!(stack.getItem() instanceof IItemParticleAmount))
		{
			return;
		}
		
		IItemParticleAmount item = (IItemParticleAmount) stack.getItem();

		CompoundTag nbt = stack.getOrCreateTag();
		
		if (!nbt.contains("particle_storage"))
		{
			CompoundTag storage =  new CompoundTag();
			storage.putInt("particle_amount", moleAmount);
			storage.putInt("particle_capacity", item.getItemCapacity(stack));
			nbt.put("particle_storage",storage);
			stack.setTag(nbt);
		}
		else if(!nbt.getCompound("particle_storage").contains("particle_capacity"))
		{
			nbt.getCompound("particle_storage").putInt("particle_capacity", item.getItemCapacity(stack));
			stack.setTag(nbt);
		}
	}

	default ParticleStack getParticleStack(ItemStack stack) {
		return new ParticleStack(getParticle(stack), 10000, 0, 0);
	}
}
