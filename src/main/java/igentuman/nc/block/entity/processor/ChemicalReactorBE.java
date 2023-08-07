package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ChemicalReactorBE extends NCProcessorBE {
    public ChemicalReactorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.CHEMICAL_REACTOR);
    }
    @Override
    public String getName() {
        return  Processors.CHEMICAL_REACTOR;
    }
}
