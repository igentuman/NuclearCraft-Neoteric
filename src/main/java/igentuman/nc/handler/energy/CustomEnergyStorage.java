package igentuman.nc.handler.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage {
    public CustomEnergyStorage(int capacity) {
        super(capacity);
    }

    public CustomEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public void setEnergyStored(int energy) {
        this.energy = energy;
    }

    public void drainEnergy(int energy) {
        setEnergyStored(this.energy - energy);
    }

    public static CustomEnergyStorage create(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        return new CustomEnergyStorage(capacity, maxReceive, maxExtract) {
            @Override
            public int receiveEnergy(int toReceive, boolean simulate) {
                int result = super.receiveEnergy(toReceive, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }

            @Override
            public int extractEnergy(int toExtract, boolean simulate) {
                int result = super.extractEnergy(toExtract, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }
        };
    }

    public static CustomEnergyStorage create(int capacity, int maxReceive, Runnable onChanged) {
        return new CustomEnergyStorage(capacity, maxReceive) {
            @Override
            public int receiveEnergy(int toReceive, boolean simulate) {
                int result = super.receiveEnergy(toReceive, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }

            @Override
            public int extractEnergy(int toExtract, boolean simulate) {
                int result = super.extractEnergy(toExtract, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }
        };
    }

    public static CustomEnergyStorage create(int capacity, Runnable onChanged) {
        return new CustomEnergyStorage(capacity) {
            @Override
            public int receiveEnergy(int toReceive, boolean simulate) {
                int result = super.receiveEnergy(toReceive, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }

            @Override
            public int extractEnergy(int toExtract, boolean simulate) {
                int result = super.extractEnergy(toExtract, simulate);
                if (!simulate && result > 0) onChanged.run();
                return result;
            }
        };
    }
}
