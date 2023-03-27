package igentuman.nc.block.entity.energy.solar;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.block.entity.processor.NCProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicSolarBE extends NCEnergy {
    public static String NAME = "solar_panel/basic";
    public BasicSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }

}
