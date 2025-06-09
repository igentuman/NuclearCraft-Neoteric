package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.energy.EnergyStorage;

import static igentuman.nc.compat.gregtech.GTUtils.tierByFe;

public class CustomEnergyStorage extends EnergyStorage {

    public boolean wasUpdated = true;
    private int receivedEnergy = 0;
    private boolean limit = false;
    private long outputAmperage;
    private long outputVoltage;
    private long inputAmperage;
    private long inputVoltage;

    public static final long[] V = new long[] { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608,
            33554432, 134217728, 536870912, 2147483648L };
    public CustomEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, 0);
    }

    public CustomEnergyStorage(int capacity, int maxTransfer, int maxExtract, long outputAmerage, long outputVoltage, long inputAmerage, long inputVoltage) {
        super(capacity, maxTransfer, maxExtract);
        this.outputAmperage = outputAmerage;
        this.outputVoltage = V[(int) outputVoltage];;
        this.inputAmperage = inputAmerage*2;
        this.inputVoltage = V[(int) inputVoltage];
    }

    public CustomEnergyStorage(int capacity, int maxTransfer, int maxExtract) {
        this(capacity, maxTransfer, maxExtract, tierByFe(maxExtract), tierByFe(maxExtract), tierByFe(maxTransfer), tierByFe(maxTransfer));
    }

    public CustomEnergyStorage(int capacity, int maxTransfer, int maxExtract, boolean limit) {
        this(capacity, maxTransfer, maxExtract);
        this.limit = limit;
    }

    public CustomEnergyStorage setInputAmperage(long value) {
        this.inputAmperage = value;
        return this;
    }

    public CustomEnergyStorage setOutputAmperage(long value) {
        this.outputAmperage = value;
        return this;
    }

    public CustomEnergyStorage setInputEnergyTier(long value) {
        this.inputAmperage = value*2;
        this.inputVoltage = V[(int) value];
        return this;
    }

    public CustomEnergyStorage setOutputEnergyTier(long value) {
        this.outputAmperage = value;
        this.outputVoltage = V[(int) value];
        return this;
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

    public long getGTOutputAmperage() {
        return outputAmperage;
    }

    public long getGTOuputVoltage() {
        return outputVoltage;
    }

    public long getGTInputAmperage() {
        return inputAmperage;
    }

    public long getGTInputVoltage() {
        return inputVoltage;
    }
}