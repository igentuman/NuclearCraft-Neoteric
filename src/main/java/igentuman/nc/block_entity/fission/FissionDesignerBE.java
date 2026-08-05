package igentuman.nc.block_entity.fission;

import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FissionDesignerBE extends BlockEntity {

    public FissionDesignerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state);
    }
}
