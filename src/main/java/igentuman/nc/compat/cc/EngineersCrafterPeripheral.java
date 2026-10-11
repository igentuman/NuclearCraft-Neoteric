package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.compat.computer.EngineersCrafterApi;

public class EngineersCrafterPeripheral extends EngineersCrafterApi implements NcPeripheral {

    public EngineersCrafterPeripheral(EngineersCrafterBE be) {
        super(be, LUA_ERRORS);
    }
}
