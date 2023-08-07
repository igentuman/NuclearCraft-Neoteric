package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IsotopeSeparatorBE extends NCProcessorBE {
    public IsotopeSeparatorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ISOTOPE_SEPARATOR);
    }
    @Override
    public String getName() {
        return Processors.ISOTOPE_SEPARATOR;
    }
}
