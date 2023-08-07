package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class QuantumTransformerBE extends NCProcessorBE {
    public QuantumTransformerBE(BlockPos pPos, BlockState pBlockState)  { super(pPos, pBlockState, Processors.QUANTUM_TRANSFORMER); }
    @Override
    public String getName() {
        return Processors.QUANTUM_TRANSFORMER;
    }
}
