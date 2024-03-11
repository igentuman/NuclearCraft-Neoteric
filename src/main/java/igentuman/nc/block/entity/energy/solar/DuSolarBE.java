package igentuman.nc.block.entity.energy.solar;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class DuSolarBE extends AbstractSolarPanel {
    public static String NAME = "solar_panel/du";
    public DuSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }

}
