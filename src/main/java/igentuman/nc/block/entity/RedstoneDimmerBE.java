package igentuman.nc.block.entity;

import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.block.RedstoneDimmerBlock.HORIZONTAL_FACING;
import static igentuman.nc.block.RedstoneDimmerBlock.LEVEL;
import static igentuman.nc.setup.registration.NCBlocks.REDSTONE_DIMMER_BE;

public class RedstoneDimmerBE extends NuclearCraftBE {

    @NBTField
    public int output = 0;

    @NBTField
    private int lastLeft = 0;

    @NBTField
    private int lastRight = 0;

    private Direction facing;

    public RedstoneDimmerBE(BlockPos pos, BlockState pBlockState) {
        super(REDSTONE_DIMMER_BE.get(), pos, pBlockState);
    }

    public void tickClient() {
    }

    public void tickServer() {
        if (getLevel().getGameTime() % 2 == 0) {
            return;
        }
        facing = getBlockState().getValue(HORIZONTAL_FACING);
        int wasOutput = output;
        int left = getLeftSignal();
        int right = getRightSignal();
        if(lastLeft == 0 && left > 0) {
            output--;
            output = Math.max(0, output);
        }
        if(lastRight == 0 && right > 0) {
            output++;
            output = Math.min(15, output);
        }
        lastLeft = left;
        lastRight = right;
        if(wasOutput != output) {
            setChanged();
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(LEVEL, output));
            getLevel().updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            BlockPos backPos = getBlockPos().relative(facing.getOpposite());
            getLevel().neighborChanged(backPos, getBlockState().getBlock(), getBlockPos());
            getLevel().updateNeighborsAtExceptFromFacing(backPos, getBlockState().getBlock(), facing);
        }
    }

    private int getRightSignal() {
        return getLevel().getSignal(getBlockPos().relative(facing.getCounterClockWise()), facing);
    }

    private int getLeftSignal() {
        return getLevel().getSignal(getBlockPos().relative(facing.getClockWise()), facing);
    }
}
