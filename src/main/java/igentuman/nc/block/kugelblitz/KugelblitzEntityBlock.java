package igentuman.nc.block.kugelblitz;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class KugelblitzEntityBlock extends BaseEntityBlock {

    private MapCodec<KugelblitzEntityBlock> codec;
    protected final String name;
    private final boolean track;

    public KugelblitzEntityBlock(BlockBehaviour.Properties props, String name, boolean track) {
        super(props);
        this.name = name;
        this.track = track;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new KugelblitzEntityBlock(props, name, track));
        }
        return codec;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get(name).blockEntity().get().create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof GlobalBlockEntity g) g.clientTick();
            };
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof GlobalBlockEntity g) g.serverTick();
        };
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (track) MultiblockHandler.trackBlockChange(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (track) MultiblockHandler.trackBlockChange(level, pos, newState);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        if (track) MultiblockHandler.trackBlockChange(level, neighborPos, level.getBlockState(neighborPos));
    }
}
