package igentuman.nc.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage {

    public boolean wasUpdated = true;
    public CustomEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer, 0);
    }
    public CustomEnergyStorage(int capacity, int maxTransfer, int maxExtract) {
        super(capacity, maxTransfer, maxExtract);
    }

    protected void onEnergyChanged() {
        wasUpdated = true;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int rc = super.receiveEnergy(maxReceive, simulate);
        if (rc > 0 && !simulate) {
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
        this.energy = energy;
        this.energy = Math.max(this.energy, 0);
        this.energy = Math.min(this.energy, getMaxEnergyStored());
        if(energy != 0) {
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
        capacity = cap;
    }

    public void setMaxExtract(int i) {
        maxExtract = i;
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("energy", energy);
        nbt.putInt("capacity", capacity);
        nbt.putInt("maxExtract", maxExtract);
        return nbt;
    }

    public void deserializeNBT(CompoundNBT energyStorage) {
        if (energyStorage.contains("energy")) {
            energy = energyStorage.getInt("energy");
        }
        if (energyStorage.contains("capacity")) {
            capacity = energyStorage.getInt("capacity");
        }
        if (energyStorage.contains("maxExtract")) {
            maxExtract = energyStorage.getInt("maxExtract");
        }
    }
}