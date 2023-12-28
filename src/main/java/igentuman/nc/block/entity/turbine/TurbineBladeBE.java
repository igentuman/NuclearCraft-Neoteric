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

public class TurbineBladeBE extends TurbineBE {
    public static String NAME = "turbine_blade";
    private BladeDef def;

    public TurbineBladeBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(multiblock() != null) {
            if (refreshCacheFlag) {
                for (Direction dir : Direction.values()) {
                    //BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                }
                refreshCacheFlag = false;
            }
        }
    }

    public void setBladeDef(BladeDef def) {
        this.def = def;
    }
}
