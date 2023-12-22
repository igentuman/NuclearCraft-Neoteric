package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TurbineCoilBE extends TurbineBE {
    public static String NAME = "turbine_coil";
    public CoilDef def;
    public boolean isValid = false;

    public double efficiency;

    public TurbineCoilBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public boolean isValid(boolean forceCheck)
    {
        if(forceCheck) {
            isValid = def().getValidator().isValid(this);
            refreshCacheFlag = true;
        }
       return isValid();
    }

    private CoilDef def() {
        if(def == null) {
            setCoilDef(TurbineRegistration.coils().get(getBlockState().getBlock().asItem().toString().replaceAll("turbine_|_coil", "")));
        }
        return def;
    }

    private boolean isValid() {
        return isValid;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(multiblock() != null) {
            if (refreshCacheFlag) {
                for (Direction dir : Direction.values()) {
                  //  BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));

                }
                isValid(true);
                refreshCacheFlag = false;
            }
        }
    }

    public void setCoilDef(CoilDef def) {
        this.def = def;
        this.efficiency = def.getEfficiency();
    }

    public double getEfficiency() {
        if(efficiency == 0) {
            efficiency = def.getEfficiency();
        }
        return efficiency;
    }
}
