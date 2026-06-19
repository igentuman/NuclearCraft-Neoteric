package igentuman.nc.compat.oc2;

import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;
import igentuman.nc.content.particles.ParticleStack;
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
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

public class RingAcceleratorDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(RingAcceleratorControllerBE blockEntity) {
        return new ObjectDevice(new RingAcceleratorRecord(blockEntity));
    }

    public record RingAcceleratorRecord(RingAcceleratorControllerBE terminal) implements NamedDevice {

        @Callback
        public final boolean isFormed() {
            return terminal.isCasingValid && terminal.isInternalValid;
        }

        @Callback
        public final String getName() {
            return terminal.getName();
        }

        @Callback
        public final boolean hasParticle() {
            return terminal.hasParticle;
        }

        @Callback
        public final int getEnergyStored() {
            return terminal.energyStorage().getEnergyStored();
        }

        @Callback
        public final int getTemperature() {
            return terminal.getTemperature();
        }

        @Callback
        public final int getMaxTemperature() {
            return terminal.maxTemperature;
        }

        @Callback
        public final int getHeatStored() {
            return terminal.heatStored;
        }

        @Callback
        public final long getHeatCapacity() {
            return terminal.heatCapacity;
        }

        @Callback
        public final int getCoolingRate() {
            return terminal.coolingRate;
        }

        @Callback
        public final int getHeatRate() {
            return terminal.heatRate;
        }

        @Callback
        public final long getAcceleratingVoltage() {
            return terminal.acceleratingVoltage;
        }

        @Callback
        public final double getDipoleStrength() {
            return terminal.dipoleStrength;
        }

        @Callback
        public final double getQuadrupoleStrength() {
            return terminal.quadStrength;
        }

        @Callback
        public final int getMinEnergy() {
            return terminal.getMinEnergy();
        }

        @Callback
        public final boolean isAcceleratorOn() {
            return terminal.controllerEnabled;
        }

        @Callback
        public final Map<String, Object> getParticleInfo() {
            ParticleStack particleStack = terminal.getParticleStack();
            if (!terminal.hasParticle || particleStack == null || particleStack.isEmpty() || particleStack.getParticle() == null) {
                return null;
            }
            Map<String, Object> info = new HashMap<>();
            info.put("energy", particleStack.getMeanEnergy());
            info.put("focus", particleStack.getFocus());
            info.put("amount", particleStack.getAmount());
            info.put("particle", particleStack.getParticle().getName());
            return info;
        }

        @Callback
        public final void setEnergyPercentage(double percentage) {
            if (percentage < 5) {
                percentage = 0;
            }
            if (percentage > 100) {
                percentage = 100;
            }
            terminal.externalControlled = true;
            terminal.isControlledByComputer = true;
            terminal.analogSignal = (byte) (percentage * 0.15D);
            terminal.accelerationEnergy = percentage / 100D;
        }

        @Callback
        public final void releaseControl() {
            terminal.externalControlled = false;
            terminal.isControlledByComputer = false;
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
