package igentuman.nc.block.particle;

import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ParticleChamberControllerBlock extends MultiblockControllerBlock {

    public ParticleChamberControllerBlock(Properties properties, String name,
                                          Supplier<BlockEntityType<? extends MultiblockControllerBE>> blockEntityType) {
        super(properties, name, blockEntityType);
    }
}
