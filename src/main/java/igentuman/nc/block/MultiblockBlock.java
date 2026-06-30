package igentuman.nc.block;

import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.util.ClientUtil.TRANSPARENT_BLOCKS;

/** Structural multiblock block with no block entity or GUI. Marker type keeps parts out of vanilla creative tabs. */
public class MultiblockBlock extends Block {

    public MultiblockBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && TRANSPARENT_BLOCKS.matcher(asItem().toString()).matches();
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return TRANSPARENT_BLOCKS.matcher(asItem().toString()).matches() ? 1.0F : super.getShadeBrightness(pState, pLevel, pPos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        MultiblockHandler.trackBlockChange(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        MultiblockHandler.trackBlockChange(level, pos, newState);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        MultiblockHandler.trackBlockChange(level, neighborPos, level.getBlockState(neighborPos));
    }
}
