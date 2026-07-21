package igentuman.nc.block.turbine;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class TurbineRotorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;

    private MapCodec<TurbineRotorBlock> codec;
    protected final String name;

    public TurbineRotorBlock(BlockBehaviour.Properties props, String name) {
        super(props);
        this.name = name;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new TurbineRotorBlock(props, name));
        }
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block bearing = ModEntries.get("turbine_bearing").block().get();
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.getBlock() instanceof TurbineRotorBlock) {
                return defaultBlockState().setValue(FACING, neighbor.getValue(FACING)).setValue(ACTIVE, false);
            }
        }
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(bearing)) {
                return defaultBlockState().setValue(FACING, dir.getOpposite()).setValue(ACTIVE, false);
            }
        }
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(ACTIVE, false);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get(name).blockEntity().get().create(pos, state);
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
        MultiblockHandler.trackBlockChange(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !newState.is(this) && state.hasProperty(FACING)) {
            Direction.Axis axis = state.getValue(FACING).getAxis();
            for (Direction dir : Direction.values()) {
                if (dir.getAxis() == axis) continue;
                BlockPos.MutableBlockPos p = pos.mutable();
                while (true) {
                    p.move(dir);
                    BlockState s = level.getBlockState(p);
                    if (!(s.getBlock() instanceof TurbineBladeBlock)) break;
                    if (s.getValue(TurbineBladeBlock.HIDDEN)) {
                        level.setBlock(p.immutable(), s.setValue(TurbineBladeBlock.HIDDEN, false), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
        MultiblockHandler.trackBlockChange(level, pos, newState);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        MultiblockHandler.trackBlockChange(level, neighborPos, level.getBlockState(neighborPos));
    }
}
