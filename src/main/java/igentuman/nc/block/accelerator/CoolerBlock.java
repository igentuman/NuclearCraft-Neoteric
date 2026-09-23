package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.api.multiblock.part.CoolerDef;


public class CoolerBlock extends MultiblockBlock {

    private final CoolerDef definition;

    public CoolerBlock(Properties properties, CoolerDef definition) {
        super(properties);
        this.definition = definition;
    }

    public CoolerDef definition() {
        return definition;
    }
}
