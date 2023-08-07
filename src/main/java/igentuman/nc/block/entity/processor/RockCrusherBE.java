package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RockCrusherBE extends NCProcessorBE {
    public RockCrusherBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ROCK_CRUSHER);
    }
    @Override
    public String getName() {
        return Processors.ROCK_CRUSHER;
    }
}
