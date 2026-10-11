package igentuman.nc.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstoneDimmerBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<RedstoneDimmerBlock> CODEC = simpleCodec(properties -> new RedstoneDimmerBlock());
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    public RedstoneDimmerBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LEVEL, 0)
                .setValue(LEFT, false)
                .setValue(RIGHT, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEVEL, LEFT, RIGHT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        return state
                .setValue(LEFT, isInputPowered(context.getLevel(), context.getClickedPos(), leftSide(state)))
                .setValue(RIGHT, isInputPowered(context.getLevel(), context.getClickedPos(), rightSide(state)));
    }

    private static Direction leftSide(BlockState state) {
        return state.getValue(FACING).getClockWise();
    }

    private static Direction rightSide(BlockState state) {
        return state.getValue(FACING).getCounterClockWise();
    }

    private static boolean isInputPowered(Level level, BlockPos pos, Direction side) {
        return level.getSignal(pos.relative(side), side) > 0;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean left = isInputPowered(level, pos, leftSide(state));
        boolean right = isInputPowered(level, pos, rightSide(state));
        int output = state.getValue(LEVEL);
        if (left && !state.getValue(LEFT)) {
            output = Math.max(0, output - 1);
        }
        if (right && !state.getValue(RIGHT)) {
            output = Math.min(15, output + 1);
        }
        BlockState updated = state.setValue(LEVEL, output).setValue(LEFT, left).setValue(RIGHT, right);
        if (updated == state) return;
        level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        if (output != state.getValue(LEVEL)) {
            updateOutput(level, pos, updated);
        }
    }

    private void updateOutput(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos target = pos.relative(facing.getOpposite());
        level.neighborChanged(target, this, pos);
        level.updateNeighborsAtExceptFromFacing(target, this, facing);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!movedByPiston && !state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            updateOutput(level, pos, state);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(FACING) == direction ? state.getValue(LEVEL) : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return direction != null && direction.getAxis().isHorizontal();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.nuclearcraft.redstone_dimmer.desc").withStyle(ChatFormatting.AQUA));
    }
}
