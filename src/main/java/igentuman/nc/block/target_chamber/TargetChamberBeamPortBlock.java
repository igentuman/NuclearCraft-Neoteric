package igentuman.nc.block.target_chamber;

import igentuman.api.platform.NCLevels;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.PortMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static igentuman.nc.util.StackUtils.isMultiTool;
import static igentuman.nc.util.TextUtils.*;
import static net.minecraft.network.chat.Component.translatable;

public class TargetChamberBeamPortBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty HORIZONTAL_FACING = FACING;
    public TargetChamberBeamPortBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }
    public TargetChamberBeamPortBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(PORT_MODE, PortMode.Mode.INPUT)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(TargetChamberBeamPortBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(PORT_MODE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return TARGET_CHAMBER_BE.get("target_chamber_beam_port").get().create(pPos, pState);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            if(isMultiTool(stack)) {
                PortMode.Mode mode = state.getValue(PORT_MODE);
                PortMode.Mode newMode = mode.next();
                level.setBlockAndUpdate(pos, state.setValue(PORT_MODE, newMode));
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(pos);
                player.sendSystemMessage(__("message.nc.switch_side.mode", newMode));
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof TargetChamberBeamPortBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof TargetChamberBeamPortBE tile) {
                tile.tickServer();
            }
        };
    }

    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag)
    {
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        if(level.isClientSide()) return;
        if(NCLevels.getExistingBlockEntity(level, neighbor) instanceof MultiblockControllerBE) {
            return;
        }
        MultiblockHandler.get(((Level)level).dimension()).trackBlockChange(pos);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
        MultiblockHandler.get(pLevel.dimension()).trackBlockChange(pPos, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if(level.isClientSide()) return;
        MultiblockHandler.get(level.dimension()).trackBlockChange(pos, true);
    }
}
