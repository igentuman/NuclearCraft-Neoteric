package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.fission.MsrControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;

public class MsrApi extends ControllerApi<MsrControllerBE> {

    public MsrApi(MsrControllerBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final double getDepletion() {
        return be.depletionSync / 100.0;
    }

    @LuaFunction
    @Callback
    public final double getTemperature() {
        return be.temperatureSync;
    }

    @LuaFunction
    @Callback
    public final double getReactivity() {
        return be.reactivitySync / 100.0;
    }

    @LuaFunction
    @Callback
    public final int getPebbleCount() {
        return be.pebbleCountSync;
    }

    @LuaFunction
    @Callback
    public final int getFuelCellsCount() {
        return be.fuelCellsCount;
    }

    @LuaFunction
    @Callback
    public final boolean isCritical() {
        return be.isCritical;
    }

    @LuaFunction
    @Callback
    public final int getOverheatTimer() {
        return be.overheatTimerSync;
    }

    @LuaFunction
    @Callback
    public final int getSaltInputRate() {
        return be.saltInputRate;
    }

    @LuaFunction
    @Callback
    public final int getSaltOutputRate() {
        return be.saltOutputRate;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void setSaltInputRate(int rate) {
        be.handleSliderUpdate(0, rate);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void setSaltOutputRate(int rate) {
        be.handleSliderUpdate(1, rate);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void voidFuel() {
        be.voidFuel();
    }
}
