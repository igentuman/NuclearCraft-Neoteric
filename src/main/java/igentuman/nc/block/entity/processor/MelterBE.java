package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MelterBE extends NCProcessorBE {
    public MelterBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.MELTER);
    }
    @Override
    public String getName() {
        return Processors.MELTER;
    }
}
