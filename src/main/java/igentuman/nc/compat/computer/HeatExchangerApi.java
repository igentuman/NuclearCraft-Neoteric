package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.heat_exchanger.HeatExchangerControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;

import java.util.HashMap;
import java.util.Map;

public class HeatExchangerApi extends ControllerApi<HeatExchangerControllerBE> {

    public HeatExchangerApi(HeatExchangerControllerBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("heat", be.getHeat());
        stats.put("maxHeat", be.getMaxHeat());
        stats.put("hotCycleOps", be.hotCycleOps);
        stats.put("coldCycleOps", be.coldCycleOps);
        stats.put("heatExchangers", be.heatExchangers);
        stats.put("radiators", be.radiators);
        stats.put("radiatorsEnabled", be.isRadiatorsEnabled());
        return stats;
    }

    @LuaFunction
    @Callback
    public final boolean isRadiatorsEnabled() {
        return be.isRadiatorsEnabled();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void enableRadiators() {
        be.setRadiatorsEnabled(true);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void disableRadiators() {
        be.setRadiatorsEnabled(false);
    }
}
