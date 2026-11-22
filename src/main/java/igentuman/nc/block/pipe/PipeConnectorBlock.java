package igentuman.nc.block.pipe;

import com.google.common.collect.ImmutableMap;
import igentuman.nc.api.pipe.PipeConnection;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.pipe.PipeNetworkManager;
import igentuman.nc.api.pipe.RedstoneMode;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

public class PipeConnectorBlock extends Block implements EntityBlock, TooltipBlock {

    public static final EnumProperty<PipeConnection> UP = EnumProperty.create("up", PipeConnection.class);
    public static final EnumProperty<PipeConnection> DOWN = EnumProperty.create("down", PipeConnection.class);
    public static final EnumProperty<PipeConnection> NORTH = EnumProperty.create("north", PipeConnection.class);
    public static final EnumProperty<PipeConnection> SOUTH = EnumProperty.create("south", PipeConnection.class);
    public static final EnumProperty<PipeConnection> EAST = EnumProperty.create("east", PipeConnection.class);
    public static final EnumProperty<PipeConnection> WEST = EnumProperty.create("west", PipeConnection.class);

    public static final Map<Direction, EnumProperty<PipeConnection>> PROPERTY_BY_DIRECTION = ImmutableMap.<Direction, EnumProperty<PipeConnection>>builder()
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

    public PipeConnectorBlock(Properties props) {
        super(props
                .sound(SoundType.METAL)
                .strength(1.5f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
                .setValue(UP, PipeConnection.NONE)
                .setValue(DOWN, PipeConnection.NONE)
                .setValue(NORTH, PipeConnection.NONE)
                .setValue(SOUTH, PipeConnection.NONE)
                .setValue(EAST, PipeConnection.NONE)
                .setValue(WEST, PipeConnection.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    private PipeConnection connectionFor(LevelReader level, BlockPos pos, Direction dir) {
        BlockPos np = pos.relative(dir);
        if (PipeNetworkManager.isPipeNode(level.getBlockState(np))) {
            return PipeConnection.PIPE;
        }
        if (level instanceof Level lvl) {
            Direction opp = dir.getOpposite();
            if (lvl.getCapability(Capabilities.Item.BLOCK, np, opp) != null
                    || lvl.getCapability(Capabilities.Fluid.BLOCK, np, opp) != null
                    || lvl.getCapability(Capabilities.Energy.BLOCK, np, opp) != null) {
                return PipeConnection.MACHINE;
            }
        }
        return PipeConnection.NONE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState();
        for (Direction d : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(d), connectionFor(level, pos, d));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(dir), connectionFor(level, pos, dir));
    }

    private VoxelShape shapeFor(BlockState state) {
        int index = 0;
        for (Direction d : Direction.values()) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(d)) != PipeConnection.NONE) {
                index |= 1 << d.get3DDataValue();
            }
        }
        VoxelShape shape = shapeCache[index];
        if (shape == null) {
            shape = CORE;
            for (Direction d : Direction.values()) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(d)) != PipeConnection.NONE) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get("pipe_connector").blockEntity().get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            PipeNetworkManager.get(level.dimension()).onNodeAdded(pos, true);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PipeConnectorBE connector) {
                if (player.isShiftKeyDown()) {
                    if (hit.getDirection() == Direction.DOWN) {
                        RedstoneMode rMode = connector.cycleRedstoneMode();
                        player.sendOverlayMessage(Component.literal("Pipe connector redstone: " + rMode));
                    } else {
                        ConnectorMode next = connector.cycleMode();
                        player.sendOverlayMessage(Component.literal("Pipe connector mode: " + next));
                    }
                    return InteractionResult.CONSUME;
                }
                serverPlayer.openMenu(connector, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        PipeNetworkManager.get(level.dimension()).onNodeRemoved(pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(TextUtils.applyFormat(__("block.nuclearcraft.pipe_connector.desc"), ChatFormatting.GRAY));
    }
}
