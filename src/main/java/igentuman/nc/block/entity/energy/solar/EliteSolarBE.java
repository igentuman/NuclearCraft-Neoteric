package igentuman.nc.block.entity.energy.solar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EliteSolarBE extends AbstractSolarPanel {
    public static String NAME = "solar_panel/elite";
    public EliteSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }

}
