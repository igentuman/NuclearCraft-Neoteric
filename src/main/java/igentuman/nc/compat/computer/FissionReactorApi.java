package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;

public class FissionReactorApi extends ControllerApi<FissionReactorControllerBE> {

    public FissionReactorApi(FissionReactorControllerBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @Override
    protected int energyPerTick() {
        return be.energyPerTick;
    }

    @LuaFunction
    @Callback
    public final boolean isReactorEnabled() {
        return enabled();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void enableReactor() {
        setEnabled(true);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void disableReactor() {
        setEnabled(false);
    }

    @LuaFunction
    @Callback
    public final boolean isRedstoneActivated() {
        return be.isRedstoneActivated();
    }

    @LuaFunction
    @Callback
    public final boolean isSteamMode() {
        return be.isSteamMode();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void toggleSteamMode() {
        be.requestToggleMode();
    }

    @LuaFunction
    @Callback
    public final int getSteamRate() {
        return be.steamPerTick;
    }

    @LuaFunction
    @Callback
    public final int getDepletionProgress() {
        return be.progress;
    }

    @LuaFunction
    @Callback
    public final int getReactivity() {
        return be.reactivity;
    }

    @LuaFunction
    @Callback
    public final double getHeatStored() {
        return be.heatBuffer.currentHeat;
    }

    @LuaFunction
    @Callback
    public final double getMaxHeatCapacity() {
        return be.heatBuffer.capacity;
    }

    @LuaFunction
    @Callback
    public final double getHeat() {
        return be.heatBuffer.heatPerTick;
    }

    @LuaFunction
    @Callback
    public final double getCooling() {
        return be.heatBuffer.cooldownPerTick;
    }

    @LuaFunction
    @Callback
    public final int getFuelCellsCount() {
        return be.fuelCells;
    }

    @LuaFunction
    @Callback
    public final int getHeatSinksCount() {
        return be.heatSinks;
    }

    @LuaFunction
    @Callback
    public final int getModeratorsCount() {
        return be.moderators;
    }

    @LuaFunction
    @Callback
    public final int getIrradiatorLines() {
        return be.irradiatorLines;
    }

    @LuaFunction
    @Callback
    public final int getModerationLevel() {
        return (int) Math.round(be.moderationFactor() * 100);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void setModerationLevel(int percent) {
        be.setModerationLevel(percent);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void voidFuel() {
        be.voidFuel();
    }
}
