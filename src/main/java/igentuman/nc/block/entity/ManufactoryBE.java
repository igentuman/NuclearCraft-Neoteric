package igentuman.nc.block.entity;

import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ManufactoryBE extends NCProcessor {
    public ManufactoryBE(BlockPos pPos, BlockState pBlockState) {
        super(NCProcessors.PROCESSORS_BE.get("manufactory").get(), pPos, pBlockState);
    }


}
