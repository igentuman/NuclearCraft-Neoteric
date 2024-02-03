package igentuman.nc.compat.gt;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import igentuman.nc.NuclearCraft;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.Direction;

import static igentuman.nc.handler.config.ProcessorsConfig.PROCESSOR_CONFIG;

public class NCGTEnergyHandler implements IEnergyContainer {
    private final CustomEnergyStorage energyStorage;
    private final long voltage;
    private final long amperage;

    private boolean overloaded = false;
    public NCGTEnergyHandler(CustomEnergyStorage energyStorage, long voltage, long amperage) {
        this.energyStorage = energyStorage;
        this.voltage = voltage;
        this.amperage = amperage;
    }

    public boolean isOverloaded()
    {
        return overloaded;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction direction, long voltage, long amperage) {
        NuclearCraft.LOGGER.info("GT EU input: "+voltage+" - "+amperage);
        if(amperage >= getInputAmperage() && PROCESSOR_CONFIG.GT_SUPPORT.get() == 2) return 0L;
        long canAccept = this.getEnergyCapacity() - this.getEnergyStored();
        overloaded = voltage > this.getInputVoltage() || overloaded;
        if (canAccept >= voltage) {
            long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, this.getInputAmperage()));
            addEnergy(voltage * amperesAccepted);
            NuclearCraft.LOGGER.info("GT EU input accept: " + amperesAccepted);
            return amperesAccepted;
        }
        return 0L;
    }

    @Override
    public long addEnergy(long energyToAdd) {
        long wasEnergy = energyStorage.getEnergyStored();
        energyStorage.addEnergy((int) (energyToAdd*4));
        return (energyStorage.getEnergyStored()-wasEnergy)/4;
    }

    @Override
    public boolean inputsEnergy(Direction direction) {
        return true;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long result = addEnergy(energyToAdd);
        if(energyStorage.getEnergyStored() < 0) {
            energyStorage.setEnergy(0);
        }
        return result;
    }

    @Override
    public long getEnergyStored() {
        return energyStorage.getEnergyStored()/4;
    }

    @Override
    public long getEnergyCapacity() {
        return energyStorage.getMaxEnergyStored()/4;
    }

    @Override
    public long getInputAmperage() {
        return amperage;
    } 

    @Override
    public long getInputVoltage() {
        return voltage;
    }
}
