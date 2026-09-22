package igentuman.nc.block.accelerator;

import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class AcceleratorIonSourcePortBlock extends AcceleratorPortBlock {

    public AcceleratorIonSourcePortBlock(Properties properties, String name,
                                         Supplier<BlockEntityType<? extends MultiblockPortBE>> blockEntityType) {
        super(properties, name, blockEntityType);
    }
}
