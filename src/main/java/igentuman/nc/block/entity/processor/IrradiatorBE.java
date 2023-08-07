package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IrradiatorBE extends NCProcessorBE {
    public IrradiatorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.IRRADIATOR);
    }
    @Override
    public String getName() {
        return Processors.IRRADIATOR;
    }
}
