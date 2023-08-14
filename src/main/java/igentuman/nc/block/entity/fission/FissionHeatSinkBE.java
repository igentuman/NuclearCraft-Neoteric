package igentuman.nc.block.entity.fission;

import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.multiblocks.HeatSinkDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FissionHeatSinkBE extends FissionBE {
    public static String NAME = "fission_heat_sink";
    public HeatSinkDef def;
    public boolean isValid = false;

    public double heat;

    public FissionHeatSinkBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public boolean isValid(boolean forceCheck)
    {
        if(def == null) {
            setHeatSinkDef(FissionBlocks.heatsinks().get(getBlockState().getBlock().asItem().toString().replace("_heat_sink", "")));
        }
        if(forceCheck) {
            isValid = def.getValidator().isValid(this);
            refreshCacheFlag = true;
        }
       return isValid() && isAttachedToFuelCell();
    }

    private boolean isValid() {
        return isValid;
    }

    @Override
    public void tickServer() {
        if(multiblock() != null) {
            if (attachedToFuelCell || refreshCacheFlag) {
                for (Direction dir : Direction.values()) {
                    BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                    if (be instanceof FissionBE) {
                        ((FissionBE) be).attachedToFuelCell = true;
                    }
                }
                isValid(true);
                refreshCacheFlag = false;
            }
        }
    }

    public void setHeatSinkDef(HeatSinkDef def) {
        this.def = def;
        this.heat = def.getHeat();
    }

    public double getHeat() {
        if(heat == 0) {
            heat = def.getHeat();
        }
        return heat;
    }
}
