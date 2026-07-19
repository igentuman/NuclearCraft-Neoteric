package igentuman.nc.block.kugelblitz;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.block_entity.kugelblitz.EXPLBE;
import igentuman.nc.block_entity.kugelblitz.EXPLProxyBE;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EXPLBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private MapCodec<EXPLBlock> codec;
    private final String name;

    public EXPLBlock(BlockBehaviour.Properties props, String name) {
        super(props);
        this.name = name;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide) return;
        Direction facing = state.getValue(FACING);
        if (canPlaceBody(level, pos, facing)) {
            if (level.getBlockEntity(pos) instanceof EXPLBE core) {
                placeProxyBlocks(level, pos, facing, core);
            }
        } else if (level instanceof ServerLevel serverLevel) {
            Block.popResource(serverLevel, pos, new ItemStack(state.getBlock().asItem()));
            serverLevel.destroyBlock(pos, false);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            removeProxyBlocks(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private boolean canPlaceBody(Level level, BlockPos pos, Direction facing) {
        for (BlockPos p : proxyPositions(pos, facing)) {
            if (!level.getBlockState(p).isAir()) return false;
        }
        return true;
    }

    private void placeProxyBlocks(Level level, BlockPos pos, Direction facing, EXPLBE core) {
        BlockState proxyState = ModEntries.get("expl_proxy").block().get().defaultBlockState();
        for (BlockPos p : proxyPositions(pos, facing)) {
            level.setBlock(p, proxyState, 3);
            if (level.getBlockEntity(p) instanceof EXPLProxyBE be) {
                be.setCore(core);
            }
        }
    }

    private void removeProxyBlocks(Level level, BlockPos pos, Direction facing) {
        for (BlockPos p : proxyPositions(pos, facing)) {
            if (level.getBlockEntity(p) instanceof EXPLProxyBE) {
                level.removeBlock(p, false);
            }
        }
    }

    /** Body footprint offsets relative to the core, excluding the core position itself. */
    public static List<BlockPos> proxyPositions(BlockPos core, Direction facing) {
        int[] b = footprint(facing);
        List<BlockPos> list = new ArrayList<>();
        for (int x = b[0]; x < b[3]; x++) {
            for (int y = b[1]; y < b[4]; y++) {
                for (int z = b[2]; z < b[5]; z++) {
                    BlockPos p = core.offset(x, y, z);
                    if (!p.equals(core)) list.add(p);
                }
            }
        }
        return list;
    }

    /** Returns {minX, minY, minZ, maxX, maxY, maxZ} for the emitter body relative to the core. */
    private static int[] footprint(Direction facing) {
        return switch (facing) {
            case UP -> new int[]{-1, 0, -1, 2, 4, 2};
            case DOWN -> new int[]{-1, -3, -1, 2, 1, 2};
            case NORTH -> new int[]{-1, -1, -3, 2, 2, 1};
            case SOUTH -> new int[]{-1, -1, 0, 2, 2, 4};
            case WEST -> new int[]{-3, -1, -1, 1, 2, 2};
            case EAST -> new int[]{0, -1, -1, 4, 2, 2};
        };
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new EXPLBlock(props, name));
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
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> Shapes.create(-1.00, 0.00, -1.00, 2.00, 4.00, 2.00);
            case DOWN -> Shapes.create(-1.00, -3.00, -1.00, 2.00, 1.00, 2.00);
            case NORTH -> Shapes.create(-1.00, -1.00, -3.00, 2.00, 2.00, 1.00);
            case SOUTH -> Shapes.create(-1.00, -1.00, 0.00, 2.00, 2.00, 4.00);
            case WEST -> Shapes.create(-3.00, -1.00, -1.00, 1.00, 2.00, 2.00);
            case EAST -> Shapes.create(0.00, -1.00, -1.00, 4.00, 2.00, 2.00);
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof EXPLBE expl) {
            serverPlayer.openMenu(expl, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, p, st, be) -> {
                if (be instanceof GlobalBlockEntity g) g.clientTick();
            };
        }
        return (lvl, p, st, be) -> {
            if (be instanceof GlobalBlockEntity g) g.serverTick();
        };
    }
}
