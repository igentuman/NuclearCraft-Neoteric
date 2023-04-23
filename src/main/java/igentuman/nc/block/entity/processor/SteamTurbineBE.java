package igentuman.nc.block.entity.processor;

import igentuman.nc.setup.processors.Processors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SteamTurbineBE extends NCProcessorBE {
    public SteamTurbineBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.STEAM_TURBINE);
    }
    @Override
    public String getName() {
        return Processors.STEAM_TURBINE;
    }
}
