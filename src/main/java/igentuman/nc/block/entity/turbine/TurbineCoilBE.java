package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.turbine.TurbineBearingBlock;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

import static net.minecraft.world.item.Items.AIR;

public class TurbineCoilBE extends TurbineBE {
    public static String NAME = "turbine_coil";
    public CoilDef def;
    @NBTField
    public boolean isValid = false;

    public double efficiency;
    @NBTField
    public boolean hasBearingConnection = false;

    public TurbineCoilBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public boolean isValid(boolean forceCheck)
    {
        if(forceCheck) {
            try {
                isValid = def().getValidator().isValid(this);
            } catch (NullPointerException ignore) {
                isValid = false;
            }
            refreshCacheFlag = true;
        }
       return isValid();
    }

    private CoilDef def() {
        Item item = getBlockState().getBlock().asItem();
        if(item.equals(AIR)) return null;
        if(def == null) {
            setCoilDef(TurbineRegistration.coils().get(item.toString().replaceAll("turbine_|_coil", "")));
        }
        return def;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(multiblock() != null) {
            if (refreshCacheFlag) {
                for (Direction dir : Direction.values()) {
                    BlockEntity be = Objects.requireNonNull(getLevel()).getBlockEntity(getBlockPos().relative(dir));
                    BlockState bs = getLevel().getBlockState(getBlockPos().relative(dir));
                    if (bs.getBlock() instanceof TurbineBearingBlock) {
                        hasBearingConnection = multiblock().bearingPositions.contains(getBlockPos().relative(dir));
                        break;
                    }
                    if (be instanceof TurbineCoilBE sideCoil) {
                        if(sideCoil.hasBearingConnection) {
                            hasBearingConnection = true;
                            break;
                        }
                    }
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
    public double getRealEfficiency() {
        if(!isValid()) return 0;
        return getEfficiency();
    }

    public void validatePlacement() {
        refreshCacheFlag = true;
        tickServer();
    }
}
