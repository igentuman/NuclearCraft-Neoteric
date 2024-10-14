package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.handler.MultiblockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FissionCasingBlock extends Block {

    public FissionCasingBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && asItem().toString().matches(".*glass|.*slope.*");
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        MultiblockHandler.trackBlockChange(pos);
    }
}
