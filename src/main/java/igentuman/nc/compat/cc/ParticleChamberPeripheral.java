package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import igentuman.nc.compat.computer.ParticleChamberApi;

public class ParticleChamberPeripheral extends ParticleChamberApi implements NcPeripheral {

    public ParticleChamberPeripheral(ParticleChamberControllerBE be) {
        super(be, LUA_ERRORS);
    }
}
