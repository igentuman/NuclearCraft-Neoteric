package igentuman.nc.api;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface ILongEnergyStorage extends IEnergyStorage {
    long receiveEnergyL(long toReceive, boolean simulate);
    long extractEnergyL(long toExtract, boolean simulate);
    long getEnergyStoredL();
    long getMaxEnergyStoredL();

    @Override default int receiveEnergy(int toReceive, boolean simulate) {
        return (int) Math.min(Integer.MAX_VALUE, receiveEnergyL(toReceive, simulate));
    }
    @Override default int extractEnergy(int toExtract, boolean simulate) {
        return (int) Math.min(Integer.MAX_VALUE, extractEnergyL(toExtract, simulate));
    }
    @Override default int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, getEnergyStoredL());
    }
    @Override default int getMaxEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, getMaxEnergyStoredL());
    }
}

