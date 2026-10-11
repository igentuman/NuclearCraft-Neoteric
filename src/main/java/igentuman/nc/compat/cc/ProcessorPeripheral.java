package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.compat.computer.ProcessorApi;

public class ProcessorPeripheral extends ProcessorApi implements NcPeripheral {

    public ProcessorPeripheral(GlobalBlockEntity be) {
        super(be, LUA_ERRORS);
    }
}
