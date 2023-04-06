package igentuman.nc.block.fission;
import igentuman.nc.phosphophyllite.modular.block.PhosphophylliteBlock;
import igentuman.nc.phosphophyllite.multiblock2.rectangular.IRectangularMultiblockBlock;
import igentuman.nc.util.annotation.NonnullDefault;
@NonnullDefault
public abstract class ReactorBaseBlock extends PhosphophylliteBlock implements IRectangularMultiblockBlock {
    
    public ReactorBaseBlock(Properties properties) {
        super(properties);
    }

}