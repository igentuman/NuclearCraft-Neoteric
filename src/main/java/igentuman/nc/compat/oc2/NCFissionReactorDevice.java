package igentuman.nc.compat.oc2;

import igentuman.nc.block.entity.fission.FissionControllerBE;
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

public class NCFissionReactorDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(FissionControllerBE<?> blockEntity) {
        return new ObjectDevice(new NCFissionReactorDeviceRecord(blockEntity));
    }

    public record NCFissionReactorDeviceRecord(FissionControllerBE<?> reactor) implements NamedDevice {

        @Callback
        public final String getName() {
            return reactor.getName();
        }

        @Callback
        public final boolean hasRecipe() {
            return reactor.hasRecipe();
        }
        @Callback
        public final boolean isSteamMode() {
            return reactor.isSteamMode;
        }

        @Callback
        public final double getSteamRate() {
            return reactor.getSteamRate();
        }

        @Callback
        public final int getDepletionProgress()
        {
            return (int) (reactor.getDepletionProgress()*100);
        }

        @Callback
        public final double getMaxHeatCapacity()
        {
            return reactor.getMaxHeat();
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
        public final void setModerationLevel(int level)
        {
            reactor.adjustModerationLevel(level);
        }

        @Callback
        public final int getEnergyStored()
        {
            return reactor.energyStorage.getEnergyStored();
        }

        @Callback
        public final double getHeatMultiplier()
        {
            return reactor.heatMultiplier;
        }

        @Callback
        public final int getModeratorsCount()
        {
            return reactor.moderatorsCount;
        }

        @Callback
        public final int getHeatSinksCount()
        {
            return reactor.heatSinksCount;
        }

        @Callback
        public final int getFuelCellsCount()
        {
            return reactor.fuelCellsCount;
        }

        @Callback
        public final int getCooling()
        {
            return (int) reactor.coolingPerTick();
        }

        @Callback
        public final int getHeat()
        {
            return (int) reactor.heatPerTick();
        }

        @Callback
        public final int getHeatStored()
        {
            return (int) reactor.heat;
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
