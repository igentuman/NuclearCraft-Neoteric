package igentuman.nc.compat.oc2;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
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

public class NCFusionReactorDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(FusionCoreBE<?> blockEntity) {
        return new ObjectDevice(new NCFusionReactorDeviceRecord(blockEntity));
    }

    public record NCFusionReactorDeviceRecord(FusionCoreBE<?> reactor) implements NamedDevice {

        @Callback
        public String getName() {
            return reactor.getName();
        }

        @Callback
        public boolean hasRecipe() {
            return reactor.hasRecipe();
        }


        @Callback
        public double getMaxHeatCapacity()
        {
            return reactor.getMaxHeat();
        }

        @Callback
        public void enableReactor()
        {
            reactor.disableForceShutdown();
        }

        @Callback
        public void disableReactor()
        {
            reactor.forceShutdown();
        }

        @Callback
        public int getEnergyPerTick()
        {
            return reactor.energyPerTick;
        }

        @Callback
        public int setRFAmplification(int amplification)
        {
            return reactor.rfAmplificationRatio = Math.min(100, Math.max(amplification, 1));
        }

        @Callback
        public int getEnergyStored()
        {
            return reactor.energyStorage.getEnergyStored();
        }

        @Callback
        public double getPlasmaStability()
        {
            return reactor.getControlPartsEfficiency();
        }


        @Callback
        public int getHeatStored()
        {
            return (int) reactor.reactorHeat;
        }

        @Callback
        public void voidFuel()
        {
            reactor.voidFuel();
        }
        
        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
