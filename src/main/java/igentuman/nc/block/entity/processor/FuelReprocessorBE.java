package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FuelReprocessorBE extends NCProcessorBE {
    public FuelReprocessorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.FUEL_REPROCESSOR);
    }
    @Override
    public String getName() {
        return Processors.FUEL_REPROCESSOR;
    }
}
