package igentuman.nc.block.entity.energy.solar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicSolarBE extends AbstractSolarPanel {
    public static String NAME = "solar_panel/basic";
    public BasicSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }

}
