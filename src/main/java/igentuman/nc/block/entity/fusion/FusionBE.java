package igentuman.nc.block.entity.fusion;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FusionBE extends NuclearCraftBE {

    @NBTField
    protected BlockPos corePos;

    protected FusionCoreBE core;

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
        if(NuclearCraft.instance.isNcBeStopped || !getLevel().getServer().isRunning()) return null;
        if(getLevel().isClientSide && corePos != null) {
            return (FusionCoreBE<?>) getLevel().getBlockEntity(corePos);
        }
        if(core == null) {
           core = (FusionCoreBE<?>) getLevel().getBlockEntity(corePos);
        }
        return core;
    }
}
