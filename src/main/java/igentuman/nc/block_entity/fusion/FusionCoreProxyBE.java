package igentuman.nc.block_entity.fusion;

import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** One of the 26 invisible cells forming the fusion core's 3x3x3 cage. Proxies item/fluid/energy
 *  capabilities to the controller so the whole core has a solid, multi-face I/O footprint. */
public class FusionCoreProxyBE extends MultiblockPortBE {

    public FusionCoreProxyBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }
}
