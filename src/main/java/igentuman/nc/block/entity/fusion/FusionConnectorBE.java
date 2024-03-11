package igentuman.nc.block.entity.fusion;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BE;

public class FusionConnectorBE extends FusionBE {
    public FusionConnectorBE(TileEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FusionConnectorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    public FusionConnectorBE() {
        super(FUSION_BE.get("fusion_connector").get());
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }

}
