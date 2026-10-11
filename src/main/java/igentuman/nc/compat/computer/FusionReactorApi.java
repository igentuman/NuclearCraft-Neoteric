package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;

public class FusionReactorApi extends ControllerApi<FusionReactorControllerBE> {

    public FusionReactorApi(FusionReactorControllerBE be, ComputerErrors errors) {
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
    public final boolean isRunning() {
        return be.isRunning();
    }

    @LuaFunction
    @Callback
    public final int getRedstoneSignal() {
        return be.inputRedstoneSignal;
    }

    @LuaFunction
    @Callback
    public final double getHeatStored() {
        return be.reactorHeat;
    }

    @LuaFunction
    @Callback
    public final double getMaxHeatCapacity() {
        return be.getMaxHeat();
    }

    @LuaFunction
    @Callback
    public final long getPlasmaTemperature() {
        return be.plasmaTemperature;
    }

    @LuaFunction
    @Callback
    public final long getMaxPlasmaTemperature() {
        return be.maxPlasmaTemperature;
    }

    @LuaFunction
    @Callback
    public final double getPlasmaStability() {
        return be.getEfficiency();
    }

    @LuaFunction
    @Callback
    public final double getMagneticFieldStrength() {
        return be.magneticFieldStrength;
    }

    @LuaFunction
    @Callback
    public final int getRFAmplification() {
        return be.amplificationAdjustment;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final int setRFAmplification(int amplification) {
        be.setAmplificationAdjustment(amplification);
        be.setChanged();
        return be.amplificationAdjustment;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void voidFuel() {
        be.voidFuel();
    }
}
