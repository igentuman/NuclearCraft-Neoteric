package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.accelerator.CoolerDef;


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
