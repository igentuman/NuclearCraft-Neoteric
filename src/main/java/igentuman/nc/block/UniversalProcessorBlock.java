package igentuman.nc.block;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/** Facing, power-lit block entity block for machine processors; opens its menu and ticks the backing BE. */
public class UniversalProcessorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static MapCodec<UniversalProcessorBlock> CODEC;
    private final String name;

    public UniversalProcessorBlock(String name) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(POWERED) ? 7 : 0));
        this.name = name;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, false);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if(CODEC == null) {
            CODEC = simpleCodec((BlockBehaviour.Properties props) -> new UniversalProcessorBlock(name));
        }
        return CODEC;
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

    private boolean isCustomModel() {
        ModEntry entry = ModEntries.get(name);
        return entry != null && entry.customModel();
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (isCustomModel()) return false;
        return super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (isCustomModel()) return Shapes.empty();
        return super.getOcclusionShape(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!ModEntries.isEnabled(name)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof UniversalProcessorBE) {
                serverPlayer.openMenu((UniversalProcessorBE) be, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof GlobalBlockEntity gBE) {
                    gBE.clientTick();
                }
            };
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof GlobalBlockEntity gBE) {
                    gBE.serverTick();
                }
            };
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GlobalBlockEntity gBE) {
                gBE.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(__("processor.description." + asItem().builtInRegistryHolder().getKey().location().getPath()).withStyle(ChatFormatting.GOLD));
    }
}
