package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NuclearFurnaceBE extends NCProcessorBE {
    public NuclearFurnaceBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.NUCLEAR_FURNACE);
    }
    @Override
    public String getName() {
        return Processors.NUCLEAR_FURNACE;
    }
}
