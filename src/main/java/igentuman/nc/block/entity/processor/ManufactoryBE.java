package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ManufactoryBE extends NCProcessorBE {
    public ManufactoryBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.MANUFACTORY);
    }

    @Override
    public String getName() {
        return Processors.MANUFACTORY;
    }
}
