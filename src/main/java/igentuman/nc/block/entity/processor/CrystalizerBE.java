package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalizerBE extends NCProcessorBE {
    public CrystalizerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState,  Processors.CRYSTALLIZER);
    }
    @Override
    public String getName() {
        return Processors.CRYSTALLIZER;
    }
}
