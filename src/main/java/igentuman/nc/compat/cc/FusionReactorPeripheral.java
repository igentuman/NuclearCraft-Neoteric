package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.compat.computer.FusionReactorApi;

public class FusionReactorPeripheral extends FusionReactorApi implements NcPeripheral {

    public FusionReactorPeripheral(FusionReactorControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
