package igentuman.nc.block.entity.energy.solar;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class AdvancedSolarBE extends AbstractSolarPanel {
    public static String NAME = "solar_panel/advanced";
    public AdvancedSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public AdvancedSolarBE() {
        super(BlockPos.ZERO, null, NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
