package igentuman.nc.block.entity.fusion;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.IMultiblockAttachable;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.multiblock.fusion.FusionReactorMultiblock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FusionBE extends NuclearCraftBE implements IMultiblockAttachable {

    @NBTField
    protected BlockPos corePos;

    protected FusionCoreBE core;
    protected FusionReactorMultiblock multiblock;

    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = (FusionReactorMultiblock) multiblock;
    }

    public FusionReactorMultiblock multiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    public FusionBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FusionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FusionReactor.FUSION_BE.get(name).get(), pPos, pBlockState);
    }

    public void tickServer() {
    }

    public void tickClient() {
    }

    public FusionCoreBE<?> controller() {

       if (NuclearCraft.instance.isNcBeStopped || (getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;

        if(getLevel().isClientSide() && corePos != null) {
            return (FusionCoreBE<?>) getLevel().getBlockEntity(corePos);
        }
        if(core == null) {
           core = (FusionCoreBE<?>) getLevel().getBlockEntity(corePos);
        }
        return core;
    }

    public void invalidateCache()
    {
        multiblock().refreshInnerCacheFlag = true;
        multiblock().refreshOuterCacheFlag = true;
        multiblock().isFormed = false;
    }

    @Override
    public void setRemoved()
    {
        if(canInvalidateCache() && !getLevel().isClientSide()) {
            if (controller() != null) controller().invalidateCache();
        }
        super.setRemoved();
    }

    public void setController(FusionCoreBE controllerBE) {
        core = controllerBE;
        corePos = controllerBE.getBlockPos();
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(multiblock() != null) {
            multiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
