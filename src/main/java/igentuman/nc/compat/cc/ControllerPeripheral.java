package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.compat.computer.ControllerApi;

public class ControllerPeripheral<T extends MultiblockControllerBE> extends ControllerApi<T> implements NcPeripheral {

    public ControllerPeripheral(T be) {
        super(be, LUA_ERRORS);
    }
}
