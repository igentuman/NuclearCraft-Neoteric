package igentuman.nc.block.turbine.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineBearingBE extends BlockEntity {
    public TurbineBearingBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public TurbineBearingBE(BlockPos pPos, BlockState pBlockState) {
        super(TURBINE_BE.get("turbine_bearing").get(), pPos, pBlockState);
    }
}
