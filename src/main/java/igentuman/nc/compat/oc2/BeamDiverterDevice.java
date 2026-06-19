package igentuman.nc.compat.oc2;

import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;
import igentuman.nc.block.beam_diverter.entity.BeamDiverterControllerBE;
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

public class BeamDiverterDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(BeamDiverterControllerBE blockEntity) {
        return new ObjectDevice(new BeamDiverterRecord(blockEntity));
    }

    public record BeamDiverterRecord(BeamDiverterControllerBE controller) implements NamedDevice {

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

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
