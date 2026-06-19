package igentuman.nc.compat.oc2;

import igentuman.nc.block.fission.entity.MSRControllerBE;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.NamedDevice;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.bus.device.rpc.RPCDevice;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class MSRDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(MSRControllerBE blockEntity) {
        return new ObjectDevice(new MSRDeviceRecord(blockEntity));
    }

    public record MSRDeviceRecord(MSRControllerBE reactor) implements NamedDevice {

        @Callback
        public final String getName() {
            return "nc_msr_reactor";
        }

        @Callback
        public final boolean isFormed() {
            return reactor.isCasingValid && reactor.isInternalValid;
        }

        @Callback
        public final boolean isCritical() {
            return reactor.isCritical;
        }

        @Callback
        public final double getTemperature() {
            return reactor.temperature;
        }

        @Callback
        public final double getPressure() {
            return reactor.pressure;
        }

        @Callback
        public final double getReactivity() {
            return reactor.reactivity;
        }

        @Callback
        public final double getImpurity() {
            return reactor.impurity;
        }

        @Callback
        public final double getSaltVolume() {
            return reactor.saltVolume;
        }

        @Callback
        public final double getCoolantVolume() {
            return reactor.coolantVolume;
        }

        @Callback
        public final double getDepletedVolume() {
            return reactor.depletedVolume;
        }

        @Callback
        public final int getPebbleCount() {
            return reactor.pebbleCount;
        }

        @Callback
        public final int getMaxPebbleCapacity() {
            return reactor.getMaxPebbleCapacity();
        }

        @Callback
        public final int getFuelCellsCount() {
            return reactor.fuelCellsCount;
        }

        @Callback
        public final int getHeatExchangerCount() {
            return reactor.heatExchangerCount;
        }

        @Callback
        public final int getEnergyPerTick() {
            return reactor.energyPerTick;
        }

        @Callback
        public final int getEnergyStored() {
            return reactor.energyStorage().getEnergyStored();
        }

        @Callback
        public final double getHeatPerTick() {
            return reactor.heatPerTick;
        }

        @Callback
        public final boolean isPortsLocked() {
            return reactor.portsLocked;
        }

        @Callback
        public final double getMaxTemperature() {
            return MSRControllerBE.MAX_TEMPERATURE;
        }

        @Callback
        public final double getMaxPressure() {
            return MSRControllerBE.PRESSURE_MAX;
        }

        @Callback
        public final void enableReactor() {
            reactor.enabledByController = true;
        }

        @Callback
        public final void disableReactor() {
            reactor.enabledByController = false;
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
