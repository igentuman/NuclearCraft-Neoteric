package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidEnricherBE extends NCProcessorBE {
    public FluidEnricherBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.FLUID_ENRICHER);
    }
    @Override
    public String getName() {
        return Processors.FLUID_ENRICHER;
    }
}
