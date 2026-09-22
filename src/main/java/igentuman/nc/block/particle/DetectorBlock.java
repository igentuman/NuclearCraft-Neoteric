package igentuman.nc.block.particle;

import igentuman.nc.multiblock.particle_chamber.DetectorDef;

public class DetectorBlock extends ParticleChamberBlock {

    private final DetectorDef definition;

    public DetectorBlock(Properties properties, DetectorDef definition) {
        super(properties);
        this.definition = definition;
    }

    public DetectorDef definition() {
        return definition;
    }
}
