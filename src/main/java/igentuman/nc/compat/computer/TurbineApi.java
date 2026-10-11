package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.turbine.TurbineControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;

public class TurbineApi extends ControllerApi<TurbineControllerBE> {

    public TurbineApi(TurbineControllerBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @Override
    protected int energyPerTick() {
        return be.energyPerTick;
    }

    @LuaFunction
    @Callback
    public final boolean isTurbineEnabled() {
        return enabled();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void enableTurbine() {
        setEnabled(true);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void disableTurbine() {
        setEnabled(false);
    }

    @LuaFunction
    @Callback
    public final double getRotationSpeed() {
        return be.rotationSpeed;
    }

    @LuaFunction
    @Callback
    public final int getFlow() {
        return be.realFlow;
    }

    @LuaFunction
    @Callback
    public final int getMaxFlow() {
        return be.maxFlow;
    }

    @LuaFunction
    @Callback
    public final int getMaxEnergyPerTick() {
        return be.maxEnergyGen;
    }

    @LuaFunction
    @Callback
    public final int getBladeCount() {
        return be.bladeCount;
    }

    @LuaFunction
    @Callback
    public final int getActiveCoils() {
        return be.activeCoils;
    }

    @LuaFunction
    @Callback
    public final int getCoilEfficiency() {
        return be.coilEfficiency;
    }
}
