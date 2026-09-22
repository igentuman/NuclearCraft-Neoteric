package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class AcceleratorControllerBlock extends MultiblockControllerBlock {

    public AcceleratorControllerBlock(Properties properties, String name,
                                      Supplier<BlockEntityType<? extends MultiblockControllerBE>> blockEntityType) {
        super(properties, name, blockEntityType);
    }
}
