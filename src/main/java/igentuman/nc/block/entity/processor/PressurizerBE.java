package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PressurizerBE extends NCProcessorBE {
    public PressurizerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.PRESSURIZER);
    }
    @Override
    public String getName() {
        return Processors.PRESSURIZER;
    }
}
