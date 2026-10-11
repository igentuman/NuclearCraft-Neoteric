package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.compat.computer.FissionReactorApi;

public class FissionReactorPeripheral extends FissionReactorApi implements NcPeripheral {

    public FissionReactorPeripheral(FissionReactorControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
