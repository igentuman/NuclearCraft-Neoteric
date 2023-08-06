package igentuman.nc.block.entity.processor;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AlloySmelterBE<RECIPE extends AbstractRecipe> extends NCProcessorBE<RECIPE> {
    public AlloySmelterBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ALLOY_SMELTER);
    }
    @Override
    public String getName() {
        return Processors.ALLOY_SMELTER;
    }

}
