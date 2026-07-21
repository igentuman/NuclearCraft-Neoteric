package igentuman.nc.block_entity.turbine;

import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TurbinePortBE extends MultiblockPortBE {

    public TurbinePortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }
}
