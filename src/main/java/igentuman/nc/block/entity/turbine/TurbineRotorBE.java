package igentuman.nc.block.entity.turbine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TurbineRotorBE extends TurbineBE {
    public static String NAME = "turbine_rotor_shaft";
    public TurbineRotorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }

}
