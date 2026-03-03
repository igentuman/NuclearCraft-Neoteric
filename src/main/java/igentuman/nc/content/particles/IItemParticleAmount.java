package igentuman.nc.content.particles;


import igentuman.api.platform.NCItemStacks;
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
        if(!sources.containsKey(stack.getItem().toString())) {
            return null;
        }
		if(sources.get(stack.getItem().toString()).getParticle() == null) {
			return null;
		}
		CompoundTag nbt = getStorageNBT(stack);
		if (!nbt.contains("particle")) {
			// Write the particle key atomically
			String particleName = sources.get(stack.getItem().toString()).getParticle().name;
			NCItemStacks.modifyTag(stack, rootTag -> {
				CompoundTag storage = rootTag.getCompound("particle_storage");
				storage.putString("particle", particleName);
				rootTag.put("particle_storage", storage);
			});
			return particles.get(particleName);
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
			int capped = Math.min(amount, getCapacity(stack));
			NCItemStacks.modifyTag(stack, rootTag -> {
				CompoundTag storage = rootTag.getCompound("particle_storage");
				storage.putInt("particle_amount", capped);
				rootTag.put("particle_storage", storage);
			});
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
		NCItemStacks.setTag(newStack, null);

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

	/**
	 * Returns a READ-ONLY copy of the "particle_storage" sub-compound.
	 * If none exists yet, returns a default in-memory compound without persisting.
	 * Use {@link #setAmountStored}, {@link #setStorageNBT}, or
	 * {@link NCItemStacks#modifyTag} for writes.
	 */
	static CompoundTag getStorageNBT(ItemStack stack)
	{
		CompoundTag nbt = NCItemStacks.getTagCopy(stack);
		if (!nbt.contains("particle_storage"))
		{
			// Return a default in-memory compound without persisting
			CompoundTag storage = new CompoundTag();
			storage.putInt("particle_amount", ParticleSources.getCapacity(stack));
			storage.putInt("particle_capacity", ParticleSources.getCapacity(stack));
			return storage;
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

		NCItemStacks.modifyTag(stack, nbt -> {
			if (!nbt.contains("particle_storage"))
			{
				CompoundTag storage = new CompoundTag();
				storage.putInt("particle_amount", moleAmount);
				storage.putInt("particle_capacity", item.getItemCapacity(stack));
				nbt.put("particle_storage", storage);
			}
			else if(!nbt.getCompound("particle_storage").contains("particle_capacity"))
			{
				nbt.getCompound("particle_storage").putInt("particle_capacity", item.getItemCapacity(stack));
			}
		});
	}

	default ParticleStack getParticleStack(ItemStack stack) {
		return new ParticleStack(getParticle(stack), 10000, 0, 0);
	}
}
