package igentuman.nc.handler.energy;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/** Native energy handler with long-valued helpers for machine-internal accounting. */
public interface ILongEnergyStorage extends EnergyHandler {
    long receiveEnergyL(long toReceive, boolean simulate);
    long extractEnergyL(long toExtract, boolean simulate);

    default long getEnergyStoredL() { return getAmountAsLong(); }
    default long getMaxEnergyStoredL() { return getCapacityAsLong(); }
    default int getEnergyStored() { return getAmountAsInt(); }
    default int getMaxEnergyStored() { return getCapacityAsInt(); }
    default int receiveEnergy(int amount, boolean simulate) {
        return (int) receiveEnergyL(amount, simulate);
    }
    default int extractEnergy(int amount, boolean simulate) {
        return (int) extractEnergyL(amount, simulate);
    }
}
