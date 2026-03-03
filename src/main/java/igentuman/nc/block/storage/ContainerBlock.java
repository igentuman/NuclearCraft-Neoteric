package igentuman.nc.block.storage;

import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCLevels;
import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.block.storage.entity.ContainerBE;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.content.storage.ContainerBlocks;
import igentuman.nc.setup.registration.NCStorageBlocks;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.StackUtils.isMultiTool;
import static igentuman.nc.util.TextUtils.__;

@NothingNullByDefault
public class ContainerBlock extends Block implements EntityBlock {

    public ContainerBlock(Properties pProperties) {
        super(pProperties);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        BlockEntity blockEntity = NCLevels.getExistingBlockEntity(pLevel, pPos);
        if (blockEntity instanceof ContainerBE containerBE) {
            return (int) ((containerBE.getLoadRate())*15);
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            ContainerBE be = (ContainerBE)NCLevels.getExistingBlockEntity(level, pos);
            if(isMultiTool(stack)) {
                Direction dirToChange = result.getDirection();
                if(player.isShiftKeyDown()) {
                    dirToChange = dirToChange.getOpposite();
                }
                SideModeToggleable.SideMode mode = be.toggleSideConfig(dirToChange.ordinal());
                player.sendSystemMessage(__("message.nc.switch_side.mode", mode.name()));
            } else {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __("container.nc.storage");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new StorageContainerContainer<>(windowId, pos, playerInventory);
                    }
                };
                ((ServerPlayer) player).openMenu(containerProvider, be.getBlockPos());
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NCStorageBlocks.STORAGE_BE.get(code()).get().create(pPos, pState);
    }

    public String code()
    {
        return asItem().toString();
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof ContainerBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof ContainerBE tile) {
                tile.tickServer();
            }
        };

    }
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (NCItemStacks.hasCustomData(stack)) {
            ContainerBE tileEntity = (ContainerBE) world.getBlockEntity(pos);
            CompoundTag nbtData = NCItemStacks.getTag(stack);
            tileEntity.loadCustomOnly(nbtData, world.registryAccess());
        }
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        ContainerBE ContainerBE = (ContainerBE) pBlockEntity;
        CompoundTag data = ContainerBE.getUpdateTag(pLevel.registryAccess());
        ItemStack drop = new ItemStack(this);
        NCItemStacks.setTag(drop, data);
        if (!pLevel.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), drop);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> list, TooltipFlag flag)
    {
        list.add(__("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
    }

    public boolean registered() {
        return ContainerBlocks.registered().containsKey(code());
    }
}
