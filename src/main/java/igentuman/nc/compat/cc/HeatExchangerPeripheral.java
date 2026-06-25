package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class HeatExchangerPeripheral implements IPeripheral {
    private final HeatExchangerControllerBE heatExchanger;

    public HeatExchangerPeripheral(HeatExchangerControllerBE heatExchanger) {
        this.heatExchanger = heatExchanger;
    }

    @Nonnull
    @Override
    public String getType() {
        return "nc_heat_exchanger";
    }

    @Override
    public boolean equals(IPeripheral other) {
        return this == other || other instanceof HeatExchangerPeripheral && ((HeatExchangerPeripheral) other).heatExchanger == heatExchanger;
    }

    @LuaFunction
    public final String getName() {
        return heatExchanger.getName();
    }

    @LuaFunction
    public final boolean isFormed() {
        return heatExchanger.isCasingValid && heatExchanger.isInternalValid;
    }

    @LuaFunction
    public final Object getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("heat", heatExchanger.heat);
        stats.put("maxHeat", heatExchanger.maxHeat);
        stats.put("hotCycleOps", heatExchanger.hotCycleOps);
        stats.put("coldCycleOps", heatExchanger.coldCycleOps);
        stats.put("radiators_qty", heatExchanger.radiators);
        return stats;
    }

    @LuaFunction(mainThread = true)
    public final void enableRadiators() {
        heatExchanger.enableRadiators();
    }

    @LuaFunction(mainThread = true)
    public final void disableRadiators() {
        heatExchanger.disableRadiators();
    }
}
