package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.compat.computer.KugelblitzApi;

public class KugelblitzPeripheral extends KugelblitzApi implements NcPeripheral {

    public KugelblitzPeripheral(ChamberTerminalBE be) {
        super(be, LUA_ERRORS);
    }
}
