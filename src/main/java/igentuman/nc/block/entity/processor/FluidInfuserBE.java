package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidInfuserBE extends NCProcessorBE {
    public FluidInfuserBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.FLUID_INFUSER);
    }
    @Override
    public String getName() {
        return Processors.FLUID_INFUSER;
    }
}
