package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DecayHastenerBE extends NCProcessorBE {
    public DecayHastenerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.DECAY_HASTENER);
    }
    @Override
    public String getName() {
        return Processors.DECAY_HASTENER;
    }
}
