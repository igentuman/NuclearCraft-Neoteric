package igentuman.nc.block.pipe;

import com.google.common.collect.ImmutableMap;
import igentuman.nc.pipe.PipeNetworkManager;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static igentuman.nc.util.TextUtils.__;

public class PipeBlock extends Block {

    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.<Direction, BooleanProperty>builder()
            .put(Direction.DOWN, DOWN)
            .put(Direction.UP, UP)
            .put(Direction.NORTH, NORTH)
            .put(Direction.SOUTH, SOUTH)
            .put(Direction.WEST, WEST)
            .put(Direction.EAST, EAST)
            .build();

    private static final VoxelShape CORE = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape[] ARMS = new VoxelShape[6];

    static {
        ARMS[Direction.DOWN.get3DDataValue()] = Block.box(4, 0, 4, 12, 4, 12);
        ARMS[Direction.UP.get3DDataValue()] = Block.box(4, 12, 4, 12, 16, 12);
        ARMS[Direction.NORTH.get3DDataValue()] = Block.box(4, 4, 0, 12, 12, 4);
        ARMS[Direction.SOUTH.get3DDataValue()] = Block.box(4, 4, 12, 12, 12, 16);
        ARMS[Direction.WEST.get3DDataValue()] = Block.box(0, 4, 4, 4, 12, 12);
        ARMS[Direction.EAST.get3DDataValue()] = Block.box(12, 4, 4, 16, 12, 12);
    }

    private final VoxelShape[] shapeCache = new VoxelShape[64];

    public PipeBlock() {
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(1.5f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    public boolean canConnectTo(BlockState neighbor) {
        Block b = neighbor.getBlock();
        return b instanceof PipeBlock || b instanceof PipeConnectorBlock;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState();
        for (Direction d : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(d), canConnectTo(level.getBlockState(pos.relative(d))));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(dir), canConnectTo(neighbor));
    }

    private VoxelShape shapeFor(BlockState state) {
        int index = 0;
        for (Direction d : Direction.values()) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(d))) {
                index |= 1 << d.get3DDataValue();
            }
        }
        VoxelShape shape = shapeCache[index];
        if (shape == null) {
            shape = CORE;
            for (Direction d : Direction.values()) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(d))) {
                    shape = Shapes.or(shape, ARMS[d.get3DDataValue()]);
                }
            }
            shapeCache[index] = shape;
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            PipeNetworkManager.get(level.dimension()).onNodeAdded(pos, false);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            PipeNetworkManager.get(level.dimension()).onNodeRemoved(pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        list.add(TextUtils.applyFormat(__("block.nuclearcraft.pipe.desc"), ChatFormatting.GRAY));
    }
}
