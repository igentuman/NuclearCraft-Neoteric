package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FissionFuelCellBE extends FissionBE {
    public static String NAME = "fission_reactor_fuel_cell";

    public FissionFuelCellBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public void tickClient() {
    }

    public void tickServer() {

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

}
