package igentuman.nc.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Structural multiblock block with no block entity or GUI. Marker type keeps parts out of vanilla creative tabs. */
public class MultiblockBlock extends Block {

    public MultiblockBlock(BlockBehaviour.Properties props) {
        super(props);
    }
}
