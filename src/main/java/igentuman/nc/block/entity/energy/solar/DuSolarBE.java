package igentuman.nc.block.entity.energy.solar;

import igentuman.nc.block.entity.energy.NCEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DuSolarBE extends NCEnergy {
    public static String NAME = "solar_panel/du";
    public DuSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }

}
