package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage {

    public boolean wasUpdated = true;
    private int receivedEnergy = 0;
    private boolean limit = false;

    public CustomEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, 0);
    }
    public CustomEnergyStorage(int capacity, int maxTransfer, int maxExtract) {
        super(capacity, maxTransfer, maxExtract);
    }
    public CustomEnergyStorage(int capacity, int maxTransfer, int maxExtract, boolean limit) {
        this(capacity, maxTransfer, maxExtract);
        this.limit = limit;
    }

    protected void onEnergyChanged() {
        wasUpdated = true;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (limit && receivedEnergy >= maxReceive) {
            return 0;
        }
        int rc = super.receiveEnergy(maxReceive, simulate);
        if (rc > 0 && !simulate) {
            receivedEnergy += rc;
            onEnergyChanged();
        }
        return rc;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int rc = super.extractEnergy(maxExtract, simulate);
        if (rc > 0 && !simulate) {
            onEnergyChanged();
        }
        return rc;
    }

    public void setEnergy(int energy) {
        int wasEnergy = this.energy;
        this.energy = energy;
        this.energy = Math.max(this.energy, 0);
        this.energy = Math.min(this.energy, getMaxEnergyStored());
        if(energy != wasEnergy) {
            onEnergyChanged();
        }
    }

    public void addEnergy(int energy) {
        this.energy += energy;
        if (this.energy > getMaxEnergyStored()) {
            this.energy = getEnergyStored();
        }
        this.energy = Math.max(this.energy, 0);
        this.energy = Math.min(this.energy, getMaxEnergyStored());
        if(energy != 0) {
            onEnergyChanged();
        }
    }

    public void consumeEnergy(int energy) {
        this.energy -= energy;
        if (this.energy < 0) {
            this.energy = 0;
        }
        if(energy != 0) {
            onEnergyChanged();
        }
    }

    public void setMaxCapacity(int cap) {
        if(cap != capacity) {
            onEnergyChanged();
        }
        capacity = cap;
    }

    public void setMaxExtract(int i) {
        if(i != maxExtract) {
            onEnergyChanged();
        }
        maxExtract = i;
    }

    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", this.getEnergyStored());
        tag.putInt("capacity", this.getMaxEnergyStored());
        return tag;
    }

    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof IntTag intNbt) {
            this.energy = intNbt.getAsInt();
        } else {
            energy = ((CompoundTag) nbt).getInt("energy");
            capacity = ((CompoundTag) nbt).getInt("capacity");
        }
    }

    public void tick() {
        receivedEnergy = 0;
    }
}