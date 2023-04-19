package igentuman.nc.block.entity.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidEnricherBE extends NCProcessorBE {
    public static String NAME = "fluid_enricher";
    public FluidEnricherBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }
}
