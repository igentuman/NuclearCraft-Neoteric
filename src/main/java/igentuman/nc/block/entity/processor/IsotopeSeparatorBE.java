package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IsotopeSeparatorBE extends NCProcessor {
    public static String NAME = "isotope_separator";
    public IsotopeSeparatorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }
}
