package igentuman.nc.block;

import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MultiblockBlock extends Block {

    public static final Pattern TRANSPARENT_BLOCKS = Pattern.compile(".*glass|.*cell.*|photon.*|.*stabilizer.*");

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
        MultiblockHandler.trackBlockChange(level, pos, oldState, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        MultiblockHandler.trackBlockChange(level, pos, state, level.getBlockState(pos));
    }

}
