package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.multiblock.turbine.BladeDef;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;

public class TurbineBladeBE extends TurbineBE {
    public static String NAME = "turbine_blade";
    private BladeDef def;

    public TurbineBladeBE() {
        this(BlockPos.ZERO, null);
    }
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
                    //TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                }
                refreshCacheFlag = false;
            }
        }
    }

    public void setBladeDef(BladeDef def) {
        this.def = def;
    }
}
