package igentuman.nc.compat.oc2;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
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

public class EngineersCrafterDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(EngineersCrafterBE blockEntity) {
        return new ObjectDevice(new EngineersCrafterDeviceRecord(blockEntity));
    }

    public record EngineersCrafterDeviceRecord(EngineersCrafterBE crafter) implements NamedDevice {

        @Callback
        public String getName() {
            return crafter.getName();
        }

        @Callback
        public int getInventorySlots() {
            return crafter.getInventorySlots();
        }

        @Callback
        public Object getSlotData(int id) {
            return crafter.getSlotData(id);
        }

        @Callback
        public Object[] getPatterns() {
            return crafter.getPatternsInfo();
        }

        @Callback(synchronize = true)
        public boolean doCrafting(int id, int qty) {
            return crafter.startCraft(id, qty);
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
