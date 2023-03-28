package igentuman.nc.block.entity.energy.solar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedSolarBE extends AbstractSolarPanel {
    public static String NAME = "solar_panel/advanced";
    public AdvancedSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }
}
