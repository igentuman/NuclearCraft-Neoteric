package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtractorBE extends NCProcessorBE {
    public ExtractorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.EXTRACTOR);
    }
    @Override
    public String getName() {
        return Processors.EXTRACTOR;
    }
}
