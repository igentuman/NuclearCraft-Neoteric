package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

import static igentuman.nc.block.entity.fission.FissionControllerBE.isModerator;

public class FissionFuelCellBE extends FissionBE {
    public static String NAME = "fission_reactor_fuel_cell";

    public FissionFuelCellBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public void tickClient() {
    }

    public int attachedModerators = 0;

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

    public int getAttachedModeratorsCount() {
        attachedModerators = 0; //todo add caching and invalidation
        for (Direction dir : Direction.values()) {
            if (isModerator(getBlockPos().relative(dir), getLevel())) {
                attachedModerators++;
            }
        }
        return attachedModerators;
    }
}
