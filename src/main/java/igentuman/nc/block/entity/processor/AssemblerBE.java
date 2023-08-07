package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblerBE extends NCProcessorBE {
    public AssemblerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ASSEMBLER);
    }
    @Override
    public String getName() {
        return Processors.ASSEMBLER;
    }
}
