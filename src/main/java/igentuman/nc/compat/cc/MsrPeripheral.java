package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.fission.MsrControllerBE;
import igentuman.nc.compat.computer.MsrApi;

public class MsrPeripheral extends MsrApi implements NcPeripheral {

    public MsrPeripheral(MsrControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
