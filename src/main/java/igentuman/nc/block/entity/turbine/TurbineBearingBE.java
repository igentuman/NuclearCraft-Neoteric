package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.turbine.TurbineRotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TurbineBearingBE extends TurbineBE {
    public static String NAME = "turbine_bearing";
    public TurbineBearingBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }
}
