package igentuman.nc.compat.gregtech;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

public class GTEnergyContainer implements IEnergyContainer {

    private static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }


    protected final CustomEnergyStorage feStorage;
    protected Direction side;
    private long feBuffer;
    private NuclearCraftBE owner;

    public GTEnergyContainer(CustomEnergyStorage feStorage, Direction side, NuclearCraftBE tile) {
        this.feStorage = feStorage;
        this.side = side;
        this.owner = tile;
    }

    public static LazyOptional<GTEnergyContainer> wrapped(CustomEnergyStorage feStorage, Direction side, NuclearCraftBE tile) {
        return LazyOptional.of(() -> new GTEnergyContainer(feStorage, side, tile));
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if(voltage > getInputVoltage()) {
            owner.handleOverVoltage();
        }
        int receive = 0;
        if (feBuffer > 0) {

            receive = feStorage.receiveEnergy(saturatedCast(feBuffer), true);

            if (receive == 0)
                return 0;

            // Internal Buffer could provide the max RF the consumer could consume
            if (feBuffer > receive) {
                feBuffer -= receive;
                feStorage.receiveEnergy(receive, false);
                return 0;

                // Buffer could not provide max value, save the remainder and continue processing
            } else {
                receive = saturatedCast(feBuffer);
                feBuffer = 0;
            }
        }

        long maxPacket = PlatformEnergyCompat.toNativeLong(voltage, PlatformEnergyCompat.ratio(false));
        long maximalValue = maxPacket * amperage;

        // Try to consume our remainder buffer plus a fresh packet
        if (receive != 0) {

            int consumable = feStorage.receiveEnergy(saturatedCast(maximalValue + receive), true);

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
            int ampsToConsume = saturatedCast((newPower / maxPacket) + 1);
            feBuffer = saturatedCast((maxPacket * ampsToConsume) - consumable);
            feStorage.receiveEnergy(consumable, false);
            return ampsToConsume;

            // Else try to draw 1 full packet
        } else {

            int consumable = feStorage.receiveEnergy(saturatedCast(maximalValue), true);

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
            int ampsToConsume = saturatedCast((consumable / maxPacket) + 1);
            feBuffer = saturatedCast((maxPacket * ampsToConsume) - consumable);
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
        return feStorage.canExtract();
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
        // 1.19.2: toFe renamed to toNative (returns int)
        feStorage.addEnergy(PlatformEnergyCompat.toNative(energyToAdd, PlatformEnergyCompat.ratio(false)));
        long newEU = getEnergyStored();
        return newEU - wasEU;
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        // 1.19.2: toFe renamed to toNative (returns int)
        return feStorage.extractEnergy(PlatformEnergyCompat.toNative(energyToRemove, PlatformEnergyCompat.ratio(false)), false);
    }

    @Override
    public long getEnergyCanBeInserted() {
        return IEnergyContainer.super.getEnergyCanBeInserted();
    }

    @Override
    public long getEnergyStored() {
        return PlatformEnergyCompat.toEu(feStorage.getEnergyStored(), PlatformEnergyCompat.ratio(true));
    }

    @Override
    public long getEnergyCapacity() {
        return PlatformEnergyCompat.toEu(feStorage.getMaxEnergyStored(), PlatformEnergyCompat.ratio(true));
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
