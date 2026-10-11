package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.heat_exchanger.HeatExchangerControllerBE;
import igentuman.nc.compat.computer.HeatExchangerApi;

public class HeatExchangerPeripheral extends HeatExchangerApi implements NcPeripheral {

    public HeatExchangerPeripheral(HeatExchangerControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
