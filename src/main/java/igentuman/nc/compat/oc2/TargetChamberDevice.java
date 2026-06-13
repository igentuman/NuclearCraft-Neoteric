package igentuman.nc.compat.oc2;

import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
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

public class TargetChamberDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(TargetChamberControllerBE blockEntity) {
        return new ObjectDevice(new NCFissionReactorDeviceRecord(blockEntity));
    }

    public record NCFissionReactorDeviceRecord(TargetChamberControllerBE reactor) implements NamedDevice {

        @Callback
        public final String getName() {
            return reactor.getName();
        }

        @Callback
        public final boolean isFormed() {
            return reactor.isCasingValid && reactor.isInternalValid;
        }

        @Callback
        public final boolean hasRecipe() {
            return reactor.hasRecipe();
        }

        @Callback
        public final int getDepletionProgress()
        {
            return (int) (reactor.getRecipeProgress()*100);
        }

        @Callback
        public final void enableReactor()
        {
            reactor.disableForceShutdown();
        }

        @Callback
        public final void disableReactor()
        {
            reactor.forceShutdown();
        }

        @Callback
        public final int getEnergyPerTick()
        {
            return reactor.energyPerTick;
        }

        @Callback
        public final int getEnergyStored()
        {
            return reactor.energyStorage().getEnergyStored();
        }

        @Callback
        public final void voidFuel()
        {
            reactor.voidFuel();
        }

        @Callback
        public final Object[] getFuelInSlot()
        {
            return reactor.getFuel();
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
