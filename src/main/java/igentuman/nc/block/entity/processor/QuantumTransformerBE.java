package igentuman.nc.block.entity.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class QuantumTransformerBE extends NCProcessorBE {
    public static String NAME = "quantum_transformer";
    public QuantumTransformerBE(BlockPos pPos, BlockState pBlockState)  { super(pPos, pBlockState, NAME); }
    @Override
    public String getName() {
        return NAME;
    }
}
