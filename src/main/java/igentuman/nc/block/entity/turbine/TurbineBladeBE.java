package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.multiblock.turbine.BladeDef;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.block.turbine.TurbineBladeBlock.HIDDEN;

public class TurbineBladeBE extends TurbineBE {
    public static String NAME = "turbine_blade";
    private BladeDef def;
    public boolean isActive = false;

    public TurbineBladeBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        boolean wasActive = isActive;
        if(multiblock() != null) {
            if (refreshCacheFlag) {
                for (Direction dir : Direction.values()) {
                    //BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                }
                refreshCacheFlag = false;
            }
        }

        isActive = multiblock() != null && controller() != null && multiblock().isFormed();

        if(wasActive != isActive) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(HIDDEN, isActive));
        }
    }

    public void setBladeDef(BladeDef def) {
        this.def = def;
    }
}
