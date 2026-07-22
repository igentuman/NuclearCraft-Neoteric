package igentuman.nc.handler.energy;

import igentuman.nc.api.ILongEnergyStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNullByDefault;

/** Energy storage with direct set/drain of stored energy and factories that fire a change callback. */
@NotNullByDefault
public class LargeEnergyStorage implements ILongEnergyStorage, INBTSerializable<Tag> {

    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;

    public LargeEnergyStorage(long capacity) {
        this(capacity, capacity, capacity, 0);
    }

    public LargeEnergyStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public LargeEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public LargeEnergyStorage(long capacity, long maxReceive, long maxExtract, long energy) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.clamp(energy, 0, capacity);
    }

    public void setEnergyStored(long energy) {
        this.energy = Math.clamp(energy, 0, capacity);
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
        if (this.energy > capacity) {
            this.energy = capacity;
        }
    }

    public void drainEnergy(long energy) {
        setEnergyStored(this.energy - energy);
    }

    public void addEnergy(long energy) {
        setEnergyStored(this.energy + energy);
    }

    @Override
    public long receiveEnergyL(long toReceive, boolean simulate) {
        if (!canReceive()) return 0;
        long energyReceived = Math.min(capacity - energy, Math.min(maxReceive, toReceive));
        if (!simulate) energy += energyReceived;
        return energyReceived;
    }

    @Override
    public long extractEnergyL(long toExtract, boolean simulate) {
        if (!canExtract()) return 0;
        long energyExtracted = Math.min(energy, Math.min(maxExtract, toExtract));
        if (!simulate) energy -= energyExtracted;
        return energyExtracted;
    }

    @Override
    public long getEnergyStoredL() {
        return energy;
    }

    @Override
    public long getMaxEnergyStoredL() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }

    public static LargeEnergyStorage create(long capacity, long maxReceive, long maxExtract, Runnable onChanged) {
        return new LargeEnergyStorage(capacity, maxReceive, maxExtract) {
            @Override
            public long receiveEnergyL(long toReceive, boolean simulate) {
                long result = super.receiveEnergyL(toReceive, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }

            @Override
            public long extractEnergyL(long toExtract, boolean simulate) {
                long result = super.extractEnergyL(toExtract, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }
        };
    }

    public static LargeEnergyStorage create(long capacity, long maxReceive, Runnable onChanged) {
        return create(capacity, maxReceive, maxReceive, onChanged);
    }

    public static LargeEnergyStorage create(long capacity, Runnable onChanged) {
        return create(capacity, capacity, capacity, onChanged);
    }

    @Override
    public Tag serializeNBT(HolderLookup.Provider provider) {
        return LongTag.valueOf(energy);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, Tag nbt) {
        if (nbt instanceof LongTag longTag) {
            this.energy = longTag.getAsLong();
        }
    }
}
