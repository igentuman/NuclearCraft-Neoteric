package igentuman.nc.block.entity.energy.solar;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class BasicSolarBE extends AbstractSolarPanel {
    public static String NAME = "solar_panel/basic";
    public BasicSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public BasicSolarBE() {
        super(BlockPos.ZERO, null, NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
