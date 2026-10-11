package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.accelerator.AbstractAcceleratorControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AcceleratorApi<T extends AbstractAcceleratorControllerBE> extends BeamControllerApi<T> {

    private static final double MIN_ENERGY_PERCENTAGE = 5;

    public AcceleratorApi(T be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final boolean hasParticle() {
        return be.particleAmount > 0;
    }

    @LuaFunction
    @Callback
    public final @Nullable Map<String, Object> getParticleInfo() {
        return particleInfo(be.particleTypeId, be.particleAmount, be.particleEnergyKeV, be.particleFocusScaled / 10_000D);
    }

    @LuaFunction
    @Callback
    public final int getTemperature() {
        return be.temperatureK;
    }

    @LuaFunction
    @Callback
    public final int getMaxTemperature() {
        return be.maximumTemperatureK;
    }

    @LuaFunction
    @Callback
    public final int getCoolingRate() {
        return be.coolantHeatPerTick;
    }

    @LuaFunction
    @Callback
    public final boolean isOverheated() {
        return be.overheated;
    }

    @LuaFunction
    @Callback
    public final int getOverheatCooldown() {
        return be.overheatCooldownTicks;
    }

    @LuaFunction
    @Callback
    public final Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("voltage", be.voltage);
        stats.put("beamLength", be.beamLength);
        stats.put("quadrupoleField", be.quadrupoleField);
        stats.put("dipoleField", be.dipoleField);
        return stats;
    }

    @LuaFunction
    @Callback
    public final boolean isAcceleratorOn() {
        return be.formed && be.controlSignal > 0 && !be.overheated;
    }

    @LuaFunction
    @Callback
    public final int getControlSignal() {
        return be.controlSignal;
    }

    @LuaFunction
    @Callback
    public final boolean isComputerControlled() {
        return be.computerSignal >= 0;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void setEnergyPercentage(double percentage) {
        double clamped = percentage < MIN_ENERGY_PERCENTAGE ? 0 : Math.min(100, percentage);
        be.setComputerSignal((int) Math.round(clamped * 0.15));
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void releaseControl() {
        be.setComputerSignal(-1);
    }
}
