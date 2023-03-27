package igentuman.nc.block.entity.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ManufactoryBE extends NCProcessor {
    public static String NAME = "manufactory";
    public ManufactoryBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
