package igentuman.nc.compat.oc2;

import igentuman.nc.block.entity.processor.NCProcessorBE;
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

public class NCProcessorDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(NCProcessorBE<?> blockEntity) {
        return new ObjectDevice(new NCProcessorDeviceRecord(blockEntity));
    }

    public record NCProcessorDeviceRecord(NCProcessorBE<?> processorBE) implements NamedDevice {

        @Callback
        public String getName() {
            return processorBE.getName();
        }

        @Callback
        public boolean hasRecipe() {
            return processorBE.hasRecipe();
        }

        @Callback
        public int getRecipeProgress() {
            return processorBE.getRecipeProgress();
        }

        @Callback
        public int toggleSlotMode(int slotId, int direction) {
            return processorBE.toggleSideConfig(slotId, direction);
        }

        @Callback
        public int getSlotMode(int slotId, int direction) {
            return processorBE.getSlotMode(slotId, direction).ordinal();
        }

        @Callback
        public int getSlotsCount() {
            return processorBE.getSlotsCount();
        }

        @Callback
        public void voidSlotContent(int id) {
            processorBE.voidSlotContent(id);
        }

        @Callback
        public Object[] getSlotContent(int id) {
            return processorBE.getSlotContent(id);
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
