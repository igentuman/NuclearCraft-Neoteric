package igentuman.nc.phosphophyllite.multiblock2.rectangular;

import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblockBlock;
import igentuman.nc.util.annotation.NonnullDefault;

@NonnullDefault
public interface IRectangularMultiblockBlock extends IValidatedMultiblockBlock {
    boolean isGoodForInterior();
    
    boolean isGoodForExterior();
    
    default boolean isGoodForFrame() {
        return isGoodForExterior();
    }
    
    default boolean isGoodForCorner() {
        return isGoodForFrame();
    }
}
