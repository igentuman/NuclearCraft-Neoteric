package igentuman.nc.block.entity.fission;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.api.nc.MultiblockAttachable;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.multiblock.fission.FissionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FissionBE extends NuclearCraftBE implements MultiblockAttachable {

    @Override
    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = (FissionReactorMultiblock) multiblock;
    }

    @Override
    public FissionBE controller() {
        try {
            return (FissionBE) getMultiblock().controller().controllerBE();
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    public FissionReactorMultiblock getMultiblock() {
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
    public FissionControllerBE<?> controller;

    public FissionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FissionReactor.FISSION_BE.get(name).get(), pPos, pBlockState);
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

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(getMultiblock() != null) {
            getMultiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
