package igentuman.nc.compat.oc2;

import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
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

public class CollisionChamberDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(CollisionChamberControllerBE blockEntity) {
        return new ObjectDevice(new TargetChamberDeviceRecord(blockEntity));
    }

    public record TargetChamberDeviceRecord(CollisionChamberControllerBE controller) implements NamedDevice {

        @Callback
        public final String getName() {
            return controller.getName();
        }

        @Callback
        public final boolean isFormed() {
            return controller.isCasingValid && controller.isInternalValid;
        }

        @Callback
        public final boolean hasRecipe() {
            return controller.hasRecipe();
        }

        @Callback
        public final int getRecipeProgress() {
            return (int) (controller.getRecipeProgress() * 100);
        }

        @Callback
        public final void enableController() {
            controller.disableForceShutdown();
        }

        @Callback
        public final void disableController() {
            controller.forceShutdown();
        }

        @Callback
        public final int getEnergyPerTick() {
            return controller.energyPerTick;
        }

        @Callback
        public final int getEnergyStored() {
            return controller.energyStorage().getEnergyStored();
        }

        @Callback
        public final Object getInputParticleInfo() {
            if (!isFormed() || !controller.hasParticle || controller.getParticleStorage().getClientParticleStack() == null) {
                return null;
            }
            Map<String, Object> set = new HashMap<String, Object>();
            Map<String, Object> a = new HashMap<String, Object>();
            a.put("energy", controller.getParticleStorage().getClientParticleStack().getMeanEnergy());
            a.put("focus", controller.getParticleStorage().getClientParticleStack().getFocus());
            a.put("amount", controller.getParticleStorage().getClientParticleStack().getAmount());
            a.put("particle", controller.getParticleStorage().getClientParticleStack().getParticle().getName());
            Map<String, Object> b = new HashMap<String, Object>();
            a.put("energy", controller.getParticleStorage().getClientParticleStackB().getMeanEnergy());
            a.put("focus", controller.getParticleStorage().getClientParticleStackB().getFocus());
            a.put("amount", controller.getParticleStorage().getClientParticleStackB().getAmount());
            a.put("particle", controller.getParticleStorage().getClientParticleStackB().getParticle().getName());
            set.put("particle_1", a);
            set.put("particle_2", b);
            return set;
        }

        @Callback
        public final Object getOutputParticlesInfo() {
            if (!isFormed() || !controller.hasParticle || controller.getParticleStorage().getClientParticleStack() == null) {
                return null;
            }
            Map<String, Object> set = new HashMap<String, Object>();
            for (ParticleStack output : controller.getParticleStorage().outputParticles) {
                if (output.getParticle() == null || output.isEmpty()) {
                    continue;
                }
                Map<String, Object> particle = new HashMap<String, Object>();
                particle.put("energy", output.getMeanEnergy());
                particle.put("focus", output.getFocus());
                particle.put("amount", output.getAmount());
                particle.put("particle", output.getParticle().getName());
                set.put(output.getParticle().getName(), particle);
            }

            return set;
        }

        @Callback(synchronize = true)
        public Object getBeamPortsInfo() {
            if (!isFormed()) {
                return null;
            }
            return controller.getBeamPortsInfo();
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
