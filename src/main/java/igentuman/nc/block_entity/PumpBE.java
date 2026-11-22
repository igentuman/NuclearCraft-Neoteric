package igentuman.nc.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PumpBE extends UniversalProcessorBE {

    public PumpBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
        if (contentHandler.hasFluidCapability()) {
            contentHandler.getFluidHandler().getInternalHandler().setTankCapacity(0, 1000000);
        }
    }

    @Override
    public boolean shouldConsumeItemInputs() {
        return false;
    }

    public boolean isInSituValid() {
        if (level == null) return false;
        BlockPos pos = getBlockPos();
        for (int i = 0; i < 2; i++) {
            pos = pos.below();
            if (!level.getBlockState(pos).isSolidRender()) {
                return false;
            }
        }
        return recipeInfo.recipe == null;
    }
}
