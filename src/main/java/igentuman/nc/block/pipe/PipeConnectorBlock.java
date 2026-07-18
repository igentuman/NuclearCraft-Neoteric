package igentuman.nc.block.pipe;

import com.google.common.collect.ImmutableMap;
import igentuman.nc.block.pipe.entity.PipeConnectorBE;
import igentuman.nc.container.PipeConnectorContainer;
import igentuman.nc.pipe.ConnectorMode;
import igentuman.nc.pipe.PipeNetworkManager;
import igentuman.nc.pipe.RedstoneMode;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static igentuman.nc.setup.registration.NCBlocks.PIPE_CONNECTOR_BE;
import static igentuman.nc.util.TextUtils.__;

public class PipeConnectorBlock extends Block implements EntityBlock {

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

    public PipeConnectorBlock() {
        super(Properties.of(Material.METAL)
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

    private PipeConnection connectionFor(BlockGetter level, BlockPos pos, Direction dir) {
        BlockPos np = pos.relative(dir);
        BlockState neighbor = level.getBlockState(np);
        if (PipeNetworkManager.isPipeNode(neighbor)) {
            return PipeConnection.PIPE;
        }
        BlockEntity be = level.getBlockEntity(np);
        if (be != null) {
            Direction opp = dir.getOpposite();
            if (be.getCapability(ForgeCapabilities.ITEM_HANDLER, opp).isPresent()
                    || be.getCapability(ForgeCapabilities.FLUID_HANDLER, opp).isPresent()
                    || be.getCapability(ForgeCapabilities.ENERGY, opp).isPresent()) {
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
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return PIPE_CONNECTOR_BE.get().create(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            PipeNetworkManager.get(level.dimension()).onNodeAdded(pos, true);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PipeConnectorBE connector) {
                if (player.isShiftKeyDown()) {
                    if (hit.getDirection() == Direction.DOWN) {
                        RedstoneMode rMode = connector.cycleRedstoneMode();
                        player.displayClientMessage(Component.literal("Pipe connector redstone: " + rMode), true);
                    } else {
                        ConnectorMode next = connector.cycleMode();
                        player.displayClientMessage(Component.literal("Pipe connector mode: " + next), true);
                    }
                    return InteractionResult.CONSUME;
                }
                MenuProvider provider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __("block.nuclearcraft.pipe_connector");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        return new PipeConnectorContainer(windowId, pos, inv);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, provider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        Direction dir = adjacentDirection(pos, fromPos);
        if (dir == null) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof PipeConnectorBE connector) {
            connector.onNeighborChanged(dir);
        }
    }

    @Nullable
    private static Direction adjacentDirection(BlockPos pos, BlockPos fromPos) {
        for (Direction d : Direction.values()) {
            if (pos.relative(d).equals(fromPos)) {
                return d;
            }
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                PipeNetworkManager.get(level.dimension()).onNodeRemoved(pos);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        list.add(TextUtils.applyFormat(__("block.nuclearcraft.pipe_connector.desc"), ChatFormatting.GRAY));
    }
}
