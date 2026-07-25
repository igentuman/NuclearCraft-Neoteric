package igentuman.nc.block_entity;

import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Pump processor BE with in-situ leaching validation; checks for solid blocks below and idle recipe state. */
public class PumpBE extends UniversalProcessorBE {

    public PumpBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    /** Returns true when the pump has two solid blocks below it and is not actively processing a recipe. */
    public boolean isInSituValid() {
        if (level == null) return false;
        BlockPos pos = getBlockPos();
        for (int i = 0; i < 2; i++) {
            pos = pos.below();
            if (!level.getBlockState(pos).isSolidRender(level, pos)) {
                return false;
            }
        }
        return recipeInfo.recipe == null;
    }
}
