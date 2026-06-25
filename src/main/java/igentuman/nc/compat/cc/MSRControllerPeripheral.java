package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.fission.entity.MSRControllerBE;

import javax.annotation.Nonnull;

public class MSRControllerPeripheral implements IPeripheral {
    private final MSRControllerBE reactor;

    public MSRControllerPeripheral(MSRControllerBE reactor) {
        this.reactor = reactor;
    }

    @Nonnull
    @Override
    public String getType() {
        return "nc_msr_reactor";
    }

    @Override
    public boolean equals(IPeripheral other) {
        return this == other || other instanceof MSRControllerPeripheral && ((MSRControllerPeripheral) other).reactor == reactor;
    }

    @LuaFunction
    public final String getName() {
        return reactor.getName();
    }

    @LuaFunction
    public final double getDepletion() {
        return reactor.depletion;
    }

    @LuaFunction
    public final double getTemperature() {
        return reactor.temperature;
    }

    @LuaFunction
    public final double getReactivity() {
        return reactor.reactivity;
    }

    @LuaFunction
    public final int getSaltInputRate() {
        return reactor.saltInputRate;
    }

    @LuaFunction
    public final int getSaltOutputRate() {
        return reactor.saltOutputRate;
    }

    @LuaFunction(mainThread = true)
    public final void setSaltInputRate(int val) {
        reactor.setSaltInputRate(val);
    }

    @LuaFunction(mainThread = true)
    public final void setSaltOutputRate(int val) {
        reactor.setSaltOutputRate(val);
    }

    @LuaFunction(mainThread = true)
    public final void voidFuel() {
        reactor.voidFuel();
    }
}
