package igentuman.nc.block.entity.fission;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.IMultiblockAttachable;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FissionBE extends NuclearCraftBE implements IMultiblockAttachable {

    @Override
    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = (FissionReactorMultiblock) multiblock;
    }

    @Override
    public FissionBE controller() {
        try {
            return (FissionBE) multiblock().controller().controllerBE();
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    public FissionReactorMultiblock multiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    protected FissionReactorMultiblock multiblock;

    public static String NAME;
    public boolean refreshCacheFlag = true;
    public boolean attachedToFuelCell = false;

    public byte validationRuns = 0;

    public FissionControllerBE controller;

    public boolean hasToTouchFuelCell = true;

    public FissionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FissionReactor.MULTIBLOCK_BE.get(name).get(), pPos, pBlockState);
    }

    public void invalidateCache()
    {
        refreshCacheFlag = true;
        validationRuns = 0;
    }

    public void tickClient() {
    }

    public void tickServer() {
    }

    @Override
    public void setRemoved()
    {
        if(controller() != null) controller().invalidateCache();
        super.setRemoved();
    }

    public boolean isDirectlyAttachedToFuelCell(BlockPos ignoredPos) {
        for (Direction dir : Direction.values()) {
            if(dir.getOpposite().getNormal() == ignoredPos) {
                continue;
            }
            BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
            if (be instanceof FissionFuelCellBE) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidating = false;
    public boolean isAttachedToFuelCell() {
        if(isValidating) {
            return attachedToFuelCell;
        }
        if(refreshCacheFlag || validationRuns < 2) {
            validationRuns++;
            if(refreshCacheFlag) {
                attachedToFuelCell = false;
                validationRuns = 0;
            }
            NCBlockPos ps = NCBlockPos.of(getBlockPos());
            for (Direction dir : Direction.values()) {
                ps.revert().relative(dir);
                BlockEntity be = getLevel().getBlockEntity(ps);
                if (be instanceof FissionFuelCellBE) {
                    attachedToFuelCell = true;
                    break;
                }
                if(be instanceof FissionBE) {
                    isValidating = true;
                    boolean attached = (refreshCacheFlag ? ((FissionBE) be).isDirectlyAttachedToFuelCell(worldPosition) : ((FissionBE) be).attachedToFuelCell)
                            || attachedToFuelCell;
                    if(attached) {
                        attachedToFuelCell = true;
                        break;
                    }
                }
            }
        }
        isValidating = false;
        return attachedToFuelCell;
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(multiblock() != null) {
            multiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
