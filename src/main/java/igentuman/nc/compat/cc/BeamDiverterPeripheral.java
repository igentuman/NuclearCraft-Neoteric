package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.accelerator.BeamDiverterControllerBE;
import igentuman.nc.compat.computer.BeamDiverterApi;

public class BeamDiverterPeripheral extends BeamDiverterApi implements NcPeripheral {

    public BeamDiverterPeripheral(BeamDiverterControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
