package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ElectrolyzerBE extends NCProcessorBE {
    public ElectrolyzerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ELECTROLYZER);
    }
    @Override
    public String getName() {
        return Processors.ELECTROLYZER;
    }
}
