package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IngotFormerBE extends NCProcessorBE {

    public IngotFormerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.INGOT_FORMER);
    }
    @Override
    public String getName() {
        return Processors.INGOT_FORMER;
    }
}
