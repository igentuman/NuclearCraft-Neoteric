package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.multiblock.turbine.BladeDef;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.block.turbine.TurbineBladeBlock.HIDDEN;
import static net.minecraft.world.item.Items.AIR;

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

        isActive = multiblock() != null && controller() != null && multiblock().isFormed();

        if(wasActive != isActive || getLevel().getGameTime() % 20 == 0) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(HIDDEN, isActive));
        }
    }

    private BladeDef def() {
        Item item = getBlockState().getBlock().asItem();
        if(item.equals(AIR)) return null;
        if(def == null) {
            setBladeDef(TurbineRegistration.blades().get(item.toString().replaceAll("turbine_", "")));
        }
        return def;
    }

    public void setBladeDef(BladeDef def) {
        this.def = def;
    }

    public float getFlow() {
        return (float) (def().getEfficiency()/100D);
    }

    public boolean isValid() {
        BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(getFacing()));
        if(be instanceof TurbineRotorBE rotor) {
            return rotor.connectedToBearing;
        }
        if(be instanceof TurbineBladeBE blade) {
            return blade.isValid();
        }
        return false;
    }

    private Direction getFacing() {
        return getBlockState().getValue(TurbineBladeBlock.FACING);
    }
}
