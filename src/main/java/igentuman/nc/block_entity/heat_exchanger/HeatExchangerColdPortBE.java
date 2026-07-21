package igentuman.nc.block_entity.heat_exchanger;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class HeatExchangerColdPortBE extends MultiblockPortBE {

    public HeatExchangerColdPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        if (c instanceof HeatExchangerControllerBE hx) {
            return hx.portFluidHandler(false);
        }
        return super.getFluidHandler(side);
    }
}
