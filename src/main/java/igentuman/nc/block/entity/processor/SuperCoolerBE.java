package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SuperCoolerBE extends NCProcessorBE {
    public SuperCoolerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.SUPERCOOLER);
    }
    @Override
    public String getName() {
        return Processors.SUPERCOOLER;
    }
}
