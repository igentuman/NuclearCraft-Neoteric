package igentuman.nc.block.entity.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtractorBE extends NCProcessorBE {
    public static String NAME = "extractor";
    public ExtractorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }
}
