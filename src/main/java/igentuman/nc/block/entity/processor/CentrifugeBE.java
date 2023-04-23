package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CentrifugeBE extends NCProcessorBE {
    public CentrifugeBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.CENTRIFUGE);
    }
    @Override
    public String getName() {
        return Processors.CENTRIFUGE;
    }
}
