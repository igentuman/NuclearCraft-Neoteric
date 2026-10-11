package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.turbine.TurbineControllerBE;
import igentuman.nc.compat.computer.TurbineApi;

public class TurbinePeripheral extends TurbineApi implements NcPeripheral {

    public TurbinePeripheral(TurbineControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
