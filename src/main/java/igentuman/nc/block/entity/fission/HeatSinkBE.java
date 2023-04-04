package igentuman.nc.block.entity.fission;

import igentuman.nc.setup.multiblocks.HeatSinkDef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HeatSinkBE extends FissionBE {
    public static String NAME = "fission_heat_sink";
    public HeatSinkDef def;
    public boolean isValid = false;

    public HeatSinkBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public boolean isValid(boolean forceCheck)
    {
       isValid = def.getValidator().isValid(this);
       return isValid();
    }

    private boolean isValid() {
        return isValid;
    }

    public void tickClient() {
    }

    public void tickServer() {

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public void setHeatSinkDef(HeatSinkDef def) {
        this.def = def;
    }
}
