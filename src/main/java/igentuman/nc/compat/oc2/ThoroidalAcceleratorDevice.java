package igentuman.nc.compat.oc2;

import igentuman.nc.block.entity.accelerator.ThoroidalAcceleratorControllerBE;
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

public class ThoroidalAcceleratorDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(ThoroidalAcceleratorControllerBE blockEntity) {
        return new ObjectDevice(new NCFChamberTerminalRecord(blockEntity));
    }

    public record NCFChamberTerminalRecord(ThoroidalAcceleratorControllerBE terminal) implements NamedDevice {

        @Callback
        public final boolean isFormed() {
            return terminal.isCasingValid && terminal.isInternalValid;
        }

        @Callback
        public final String getName() {
            return "nc_linear_accelerator";
        }

        @Callback
        public final boolean hasRecipe() {
            return terminal.hasRecipe();
        }


        @Callback
        public final int getEnergyStored()
        {
            return terminal.energyStorage().getEnergyStored();
        }


        @Callback
        public final int getRecipeProgress()
        {
            return (int) (terminal.recipeInfo().getProgress() * 100);
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
