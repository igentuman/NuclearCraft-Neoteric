package igentuman.nc.compat.oc2;

import dan200.computercraft.api.lua.LuaException;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.NamedDevice;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.bus.device.rpc.RPCDevice;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class KugelblitzDevice {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(ChamberTerminalBE blockEntity) {
        return new ObjectDevice(new NCFChamberTerminalRecord(blockEntity));
    }

    public record NCFChamberTerminalRecord(ChamberTerminalBE terminal) implements NamedDevice {

        @Callback
        public final boolean isFormed() {
            return terminal.isCasingValid && terminal.isInternalValid;
        }

        @Callback
        public final String getName() {
            return "nc_kugelblitz";
        }

        @Callback
        public final boolean hasRecipe() {
            return terminal.hasRecipe();
        }

        @Callback
        public final int getEnergyPerTick()
        {
            return terminal.energyPerTick;
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

        @Callback
        public final int getEvaporationRate()
        {
            return terminal.evaporation;
        }

        @Callback
        public final int getFeedingRate()
        {
            return (int) terminal.feeding;
        }

        @Callback
        public final int getBlackholeMass() {
            return (int) (terminal.mass/1000);
        }

        @Callback
        public final int getBlackholeStability() {
            return terminal.blackholeStability;
        }

        @Callback
        public final int getQuantumFrequency() {
            return terminal.frequency;
        }

        @Callback
        public final int setQuantumFrequency(int frequency) throws LuaException {
            if (frequency < 0 || frequency > 15) {
                throw new LuaException("Frequency must be between 0 and 15");
            }
            terminal.frequency = (byte) frequency;
            terminal.setChanged();
            return terminal.frequency;
        }

        @Callback
        public final int getFluxRegulators() {
            return terminal.fluxRegulators;
        }

        @Callback
        public final int getTransformers() {
            return terminal.transformers;
        }

        @Callback
        public final int getStabilizers() {
            return terminal.stabilizers;
        }

        @Callback
        public final int getTransformationEnergyRate() {
            return terminal.energyConvertionRate;
        }

        @Callback
        public final int setTransformationEnergyRate(int rate) throws LuaException {
            if (rate < 0 || rate > 100) {
                throw new LuaException("Rate must be between 0 and 100");
            }
            terminal.energyConvertionRate = rate;
            terminal.setChanged();
            return terminal.energyConvertionRate;
        }
        
        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList(this.getName());
        }
    }
}
