package igentuman.nc.compat.oc2;

import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
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

public class HeatExchangerDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(HeatExchangerControllerBE blockEntity) {
        return new ObjectDevice(new HeatExchangerDeviceRecord(blockEntity));
    }

    public record HeatExchangerDeviceRecord(HeatExchangerControllerBE heatExchanger) implements NamedDevice {

        @Callback
        public final String getName() {
            return heatExchanger.getName();
        }

        @Callback
        public final boolean isFormed() {
            return heatExchanger.isCasingValid && heatExchanger.isInternalValid;
        }

        @Callback
        public final Map<String, Object> getStatistics() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("heat", heatExchanger.heat);
            stats.put("maxHeat", heatExchanger.maxHeat);
            stats.put("hotCycleOps", heatExchanger.hotCycleOps);
            stats.put("coldCycleOps", heatExchanger.coldCycleOps);
            stats.put("radiators_qty", heatExchanger.radiators);
            return stats;
        }

        @Callback(synchronize = true)
        public final void enableRadiators() {
            heatExchanger.enableRadiators();
        }

        @Callback(synchronize = true)
        public final void disableRadiators() {
            heatExchanger.disableRadiators();
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
