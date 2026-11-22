package igentuman.nc.block.pipe;

import com.google.common.collect.ImmutableMap;
import igentuman.nc.pipe.PipeNetworkManager;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

public class PipeBlock extends Block implements TooltipBlock {

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

    public PipeBlock(Properties props) {
        super(props
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
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            PipeNetworkManager.get(level.dimension()).onNodeAdded(pos, false);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        PipeNetworkManager.get(level.dimension()).onNodeRemoved(pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(TextUtils.applyFormat(__("block.nuclearcraft.pipe.desc"), ChatFormatting.GRAY));
    }
}
