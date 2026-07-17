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

    public record RingAcceleratorRecord(RingAcceleratorControllerBE controller) implements NamedDevice {

        @Callback
        public final boolean isFormed() {
            return controller.isCasingValid && controller.isInternalValid;
        }

        @Callback
        public final String getName() {
            return controller.getName();
        }

        @Callback
        public final boolean hasParticle() {
            return controller.hasParticle;
        }

        @Callback
        public final int getEnergyStored() {
            return controller.energyStorage().getEnergyStored();
        }

        @Callback
        public final int getTemperature() {
            return controller.getTemperature();
        }

        @Callback
        public final int getMaxTemperature() {
            return controller.maxTemperature;
        }

        @Callback
        public final int getHeatStored() {
            return controller.heatStored;
        }

        @Callback
        public final long getHeatCapacity() {
            return controller.heatCapacity;
        }

        @Callback
        public final int getCoolingRate() {
            return controller.coolingRate;
        }

        @Callback
        public final int getHeatRate() {
            return controller.heatRate;
        }

        @Callback
        public final long getAcceleratingVoltage() {
            return controller.acceleratingVoltage;
        }

        @Callback
        public final double getDipoleStrength() {
            return controller.dipoleStrength;
        }

        @Callback
        public final double getQuadrupoleStrength() {
            return controller.quadStrength;
        }

        @Callback
        public final int getMinEnergy() {
            return controller.getMinEnergy();
        }

        @Callback
        public final boolean isAcceleratorOn() {
            return controller.controllerEnabled;
        }

        @Callback
        public final Map<String, Object> getParticleInfo() {
            ParticleStack particleStack = controller.getParticleStack();
            if (!controller.hasParticle || particleStack == null || particleStack.isEmpty() || particleStack.getParticle() == null) {
                return null;
            }
            Map<String, Object> info = new HashMap<>();
            info.put("energy", particleStack.getMeanEnergy());
            info.put("focus", particleStack.getFocus());
            info.put("amount", particleStack.getAmount());
            info.put("particle", particleStack.getParticle().getName());
            return info;
        }

        @Callback(synchronize = true)
        public Object getBeamPortsInfo() {
            if (!isFormed()) {
                return null;
            }
            return controller.getBeamPortsInfo();
        }

        @Callback(synchronize = true)
        public boolean setBeamPortMode(int id, String mode) {
            if (!isFormed()) {
                return false;
            }
            return controller.setBeamPortMode(id, mode);
        }

        @Callback
        public final void setEnergyPercentage(double percentage) {
            if (percentage < 5) {
                percentage = 0;
            }
            if (percentage > 100) {
                percentage = 100;
            }
            controller.externalControlled = true;
            controller.isControlledByComputer = true;
            controller.analogSignal = (byte) (percentage * 0.15D);
            controller.accelerationEnergy = percentage / 100D;
        }

        @Callback
        public final void releaseControl() {
            controller.externalControlled = false;
            controller.isControlledByComputer = false;
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
