package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import li.cil.oc2.api.bus.device.object.Callback;

public class KugelblitzApi extends ControllerApi<ChamberTerminalBE> {

    public KugelblitzApi(ChamberTerminalBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @Override
    protected int energyPerTick() {
        return be.energyPerTick;
    }

    @LuaFunction
    @Callback
    public final boolean isBlackholeActive() {
        return be.controllerEnabled;
    }

    @LuaFunction
    @Callback
    public final long getBlackholeMass() {
        return be.mass;
    }

    @LuaFunction
    @Callback
    public final int getBlackholeStability() {
        return be.blackholeStability;
    }

    @LuaFunction
    @Callback
    public final int getEvaporationRate() {
        return be.evaporation;
    }

    @LuaFunction
    @Callback
    public final long getFeedingRate() {
        return be.feeding;
    }

    @LuaFunction
    @Callback
    public final int getFluxRegulators() {
        return be.fluxRegulators;
    }

    @LuaFunction
    @Callback
    public final int getTransformers() {
        return be.transformers;
    }

    @LuaFunction
    @Callback
    public final int getStabilizers() {
        return be.stabilizers;
    }

    @LuaFunction
    @Callback
    public final int getQuantumFrequency() {
        return be.frequency;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final int setQuantumFrequency(int frequency) {
        if (frequency < 0 || frequency > 15) throw error("Frequency must be between 0 and 15");
        be.frequency = (byte) frequency;
        be.setChanged();
        return be.frequency;
    }

    @LuaFunction
    @Callback
    public final int getTransformationEnergyRate() {
        return be.energyConvertionRate;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final int setTransformationEnergyRate(int rate) {
        if (rate < 0 || rate > 100) throw error("Rate must be between 0 and 100");
        be.energyConvertionRate = rate;
        be.setChanged();
        return be.energyConvertionRate;
    }
}
