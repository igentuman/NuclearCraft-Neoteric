package igentuman.nc.compat.gregtech;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.utils.GTMath;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

public class GTEnergyContainer implements IEnergyContainer {

    protected final CustomEnergyStorage feStorage;
    protected Direction side;
    private long feBuffer;

    public GTEnergyContainer(CustomEnergyStorage feStorage, Direction side) {
        this.feStorage = feStorage;
        this.side = side;
    }

    public static LazyOptional<GTEnergyContainer> wrapped(CustomEnergyStorage feStorage, Direction side) {
        return LazyOptional.of(() -> new GTEnergyContainer(feStorage, side));
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        int receive = 0;
        if (feBuffer > 0) {

            receive = feStorage.receiveEnergy(GTMath.saturatedCast(feBuffer), true);

            if (receive == 0)
                return 0;

            // Internal Buffer could provide the max RF the consumer could consume
            if (feBuffer > receive) {
                feBuffer -= receive;
                feStorage.receiveEnergy(receive, false);
                return 0;

                // Buffer could not provide max value, save the remainder and continue processing
            } else {
                receive = GTMath.saturatedCast(feBuffer);
                feBuffer = 0;
            }
        }

        long maxPacket = FeCompat.toFeLong(voltage, FeCompat.ratio(false));
        long maximalValue = maxPacket * amperage;

        // Try to consume our remainder buffer plus a fresh packet
        if (receive != 0) {

            int consumable = feStorage.receiveEnergy(GTMath.saturatedCast(maximalValue + receive), true);

            // Machine unable to consume any power
            if (consumable == 0)
                return 0;

            // Only able to consume our buffered amount
            if (consumable == receive) {
                feStorage.receiveEnergy(consumable, false);
                return 0;
            }

            // Able to consume our full packet as well as our remainder buffer
            if (consumable == maximalValue + receive) {
                feStorage.receiveEnergy(consumable, false);
                return amperage;
            }

            long newPower = consumable - receive;

            // Able to consume buffered amount plus an even amount of packets (no buffer needed)
            if (newPower % maxPacket == 0) {
                return feStorage.receiveEnergy(consumable, false) / maxPacket;
            }

            // Able to consume buffered amount plus some amount of power with a packet remainder
            int ampsToConsume = GTMath.saturatedCast((newPower / maxPacket) + 1);
            feBuffer = GTMath.saturatedCast((maxPacket * ampsToConsume) - consumable);
            feStorage.receiveEnergy(consumable, false);
            return ampsToConsume;

            // Else try to draw 1 full packet
        } else {

            int consumable = feStorage.receiveEnergy(GTMath.saturatedCast(maximalValue), true);

            // Machine unable to consume any power
            if (consumable == 0)
                return 0;

            // Able to accept the full amount of power
            if (consumable == maximalValue) {
                feStorage.receiveEnergy(consumable, false);
                return amperage;
            }

            // Able to consume an even amount of packets
            if (consumable % maxPacket == 0) {
                return feStorage.receiveEnergy(consumable, false) / maxPacket;
            }

            // Able to consume power with some amount of power remainder in the packet
            int ampsToConsume = GTMath.saturatedCast((consumable / maxPacket) + 1);
            feBuffer = GTMath.saturatedCast((maxPacket * ampsToConsume) - consumable);
            feStorage.receiveEnergy(consumable, false);
            return ampsToConsume;
        }
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return feStorage.canReceive();
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return feStorage.canReceive();
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        if(differenceAmount > 0) {
            return addEnergy(differenceAmount);
        } else if(differenceAmount < 0) {
            return removeEnergy(-differenceAmount);
        }
        return differenceAmount;
    }

    @Override
    public long addEnergy(long energyToAdd) {
        long wasEU = getEnergyStored();
        feStorage.addEnergy(FeCompat.toFe(energyToAdd, FeCompat.ratio(false)));
        long newEU = getEnergyStored();
        return newEU - wasEU;
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return feStorage.extractEnergy(FeCompat.toFe(energyToRemove, FeCompat.ratio(false)), false);
    }

    @Override
    public long getEnergyCanBeInserted() {
        return IEnergyContainer.super.getEnergyCanBeInserted();
    }

    @Override
    public long getEnergyStored() {
        return FeCompat.toEu(feStorage.getEnergyStored(), FeCompat.ratio(true));
    }

    @Override
    public long getEnergyCapacity() {
        return FeCompat.toEu(feStorage.getMaxEnergyStored(), FeCompat.ratio(true));
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return IEnergyContainer.super.getEnergyInfo();
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return IEnergyContainer.super.supportsBigIntEnergyValues();
    }

    @Override
    public long getOutputAmperage() {
        return feStorage.getGTOutputAmperage();
    }

    @Override
    public long getOutputVoltage() {
        return feStorage.getGTOuputVoltage();
    }

    @Override
    public long getInputAmperage() {
        return feStorage.getGTInputAmperage();
    }

    @Override
    public long getInputVoltage() {
        return feStorage.getGTInputVoltage();
    }

    @Override
    public long getInputPerSec() {
        return IEnergyContainer.super.getInputPerSec();
    }

    @Override
    public long getOutputPerSec() {
        return IEnergyContainer.super.getOutputPerSec();
    }

    @Override
    public boolean isOneProbeHidden() {
        return false;
    }
}
