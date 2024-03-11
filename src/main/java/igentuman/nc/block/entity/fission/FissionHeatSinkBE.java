package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;

public class FissionHeatSinkBE extends FissionBE {
    public static String NAME = "fission_heat_sink";
    public HeatSinkDef def;
    public boolean isValid = false;

    public double heat;

    public FissionHeatSinkBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public FissionHeatSinkBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(pPos, pBlockState, name);
    }

    public boolean isValid(boolean forceCheck)
    {
        if(forceCheck) {
            isValid = def().getValidator().isValid(this);
            refreshCacheFlag = true;
        }
       return isValid() && isAttachedToFuelCell();
    }

    private HeatSinkDef def() {
        if(def == null) {
            setHeatSinkDef(FissionBlocks.heatsinks().get(getBlockState().getBlock().asItem().toString().replace("_heat_sink", "")));
        }
        return def;
    }

    @Override
    public boolean isAttachedToFuelCell()
    {
        if(def().mustdDirectlyTouchFuelCell()) {
            return isDirectlyAttachedToFuelCell(worldPosition);
        }
        return super.isAttachedToFuelCell();
    }

    private boolean isValid() {
        return isValid;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(multiblock() != null) {
            if (attachedToFuelCell || refreshCacheFlag) {
                for (Direction dir : Direction.values()) {
                    TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
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
        hasToTouchFuelCell = def.getValidator().hasToTouchFuelCell();
    }

    public double getHeat() {
        if(heat == 0) {
            heat = def.getHeat();
        }
        return heat;
    }

    @Override
    public boolean isDirectlyAttachedToFuelCell(BlockPos ignoredPos) {
        if(def == null) return super.isDirectlyAttachedToFuelCell(ignoredPos);
        if(def.getValidator().hasToTouchFuelCell()) {
            return super.isDirectlyAttachedToFuelCell(ignoredPos);
        }
        return isAttachedToFuelCell();
    }
}
