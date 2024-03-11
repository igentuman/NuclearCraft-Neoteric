package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fission.FissionFuelCellBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.IMultiblockAttachable;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;

public class TurbineBE extends NuclearCraftBE implements IMultiblockAttachable {

    @Override
    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = (TurbineMultiblock) multiblock;
    }

    @Override
    public TurbineBE controller() {
        try {
            return (TurbineBE) multiblock().controller().controllerBE();
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    public TurbineMultiblock multiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    protected TurbineMultiblock multiblock;

    public static String NAME;
    public boolean refreshCacheFlag = true;

    public byte validationRuns = 0;

    public TurbineControllerBE<?> controller;

    public TurbineBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(TurbineRegistration.TURBINE_BE.get(name).get(), pPos, pBlockState);
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
            TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
            if (be instanceof FissionFuelCellBE) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidating = false;

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(multiblock() != null) {
            multiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
