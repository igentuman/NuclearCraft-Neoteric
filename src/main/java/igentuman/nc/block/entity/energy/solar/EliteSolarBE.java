package igentuman.nc.block.entity.energy.solar;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

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
