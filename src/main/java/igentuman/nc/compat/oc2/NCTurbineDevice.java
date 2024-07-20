package igentuman.nc.compat.oc2;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import li.cil.oc2r.api.bus.device.Device;
import li.cil.oc2r.api.bus.device.object.Callback;
import li.cil.oc2r.api.bus.device.object.NamedDevice;
import li.cil.oc2r.api.bus.device.object.ObjectDevice;
import li.cil.oc2r.api.bus.device.rpc.RPCDevice;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class NCTurbineDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(TurbineControllerBE<?> blockEntity) {
        return new ObjectDevice(new NCFTurbineDeviceRecord(blockEntity));
    }

    public record NCFTurbineDeviceRecord(TurbineControllerBE<?> turbine) implements NamedDevice {

        @Callback
        public final String getName() {
            return turbine.getName();
        }

        @Callback
        public final boolean hasRecipe() {
            return turbine.hasRecipe();
        }

        @Callback
        public final void enableTurbine()
        {
            turbine.disableForceShutdown();
        }

        @Callback
        public final void disableTurbine()
        {
            turbine.forceShutdown();
        }

        @Callback
        public final int getEnergyPerTick()
        {
            return turbine.energyPerTick;
        }


        @Callback
        public final int getEnergyStored()
        {
            return turbine.energyStorage.getEnergyStored();
        }
        
        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
