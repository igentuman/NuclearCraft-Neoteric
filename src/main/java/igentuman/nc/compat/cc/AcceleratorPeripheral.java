package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.accelerator.AbstractAcceleratorControllerBE;
import igentuman.nc.compat.computer.AcceleratorApi;

public class AcceleratorPeripheral<T extends AbstractAcceleratorControllerBE> extends AcceleratorApi<T> implements NcPeripheral {

    public AcceleratorPeripheral(T be) {
        super(be, LUA_ERRORS);
    }
}
