package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.multiblock.fission.FissionReactorMultiblock.isModerator;

public class FissionFuelCellBE extends FissionBE {
    public static String NAME = "fission_reactor_fuel_cell";

    public FissionFuelCellBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public int attachedModerators = 0;

    @Override
    public final boolean isAttachedToFuelCell() {
        return true;
    }

    public void setAttachedToFuelCell(BlockPos pos)
    {
        for (Direction dir : Direction.values()) {
            BlockEntity be = getLevel().getBlockEntity(pos.relative(dir));
            if(be instanceof FissionBE) {
                ((FissionBE) be).attachedToFuelCell = true;
            }
        }
    }
    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(refreshCacheFlag || validationRuns < 2) {
            validationRuns++;
            getAttachedModeratorsCount(false);
            if(validationRuns > 1) refreshCacheFlag = false;
        }
    }

    public int getAttachedModeratorsCount(boolean forceRefresh) {
        if(refreshCacheFlag || forceRefresh) {
            attachedModerators = 0;
            for (Direction dir : Direction.values()) {
                if (isModerator(getBlockPos().relative(dir), getLevel())) {
                    attachedModerators++;
                    setAttachedToFuelCell(getBlockPos().relative(dir));
                }
            }
        }
        return attachedModerators;
    }
}
