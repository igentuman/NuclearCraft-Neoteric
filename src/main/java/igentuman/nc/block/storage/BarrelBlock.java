package igentuman.nc.block.storage;

import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCLevels;
import igentuman.api.platform.NCNames;
import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.block.storage.entity.BarrelBE;
import igentuman.nc.setup.registration.NCStorageBlocks;
import igentuman.nc.content.storage.BarrelBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.StackUtils.isMultiTool;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatLiquid;
import static net.minecraft.world.item.Items.BUCKET;

public class BarrelBlock extends Block implements EntityBlock {

    public BarrelBlock(Properties pProperties) {
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
        if (blockEntity instanceof BarrelBE barrelBE) {
            return (int) ((barrelBE.fluidTank.getFluid().getAmount()/(double)barrelBE.fluidTank.getCapacity())*15);
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            BarrelBE be = (BarrelBE)NCLevels.getExistingBlockEntity(level, pos);
            IFluidHandler barrel = be.fluidTank;
            if(isMultiTool(stack)) {
                Direction dirToChange = result.getDirection();
                if(player.isShiftKeyDown()) {
                    dirToChange = dirToChange.getOpposite();
                }
                SideModeToggleable.SideMode mode = be.toggleSideConfig(dirToChange.ordinal());
                player.sendSystemMessage(__("message.nc.switch_side.mode", mode.name()));
            } else
            if(!stack.equals(ItemStack.EMPTY)) {
                if(stack.getItem() instanceof BucketItem) {
                    Fluid to = ((BucketItem) stack.getItem()).content;
                    if(to == null || to == FluidStack.EMPTY.getFluid()) {
                        if(!barrel.getFluidInTank(0).isEmpty() && barrel.getFluidInTank(0).getAmount() >= 1000) {
                            ItemStack bucket = new ItemStack(barrel.getFluidInTank(0).getFluid().getBucket());
                            barrel.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                            if(stack.getCount() == 1) {
                                player.setItemInHand(hand, bucket);
                            } else {
                                stack.shrink(1);
                                if(!player.getInventory().add(bucket)) {
                                    player.drop(bucket, false);
                                }
                            }
                        }
                    } else {
                        int filled = barrel.fill(new FluidStack(to, 1000), IFluidHandler.FluidAction.SIMULATE);
                        if(filled == 1000) {
                            barrel.fill(new FluidStack(to, 1000), IFluidHandler.FluidAction.EXECUTE);
                            if(player.isCreative()) return ItemInteractionResult.SUCCESS;
                            if(stack.getCount() == 1) {
                                player.setItemInHand(hand, new ItemStack(BUCKET));
                            } else {
                                stack.shrink(1);
                                if(!player.getInventory().add(new ItemStack(to.getBucket()))) {
                                    player.drop(new ItemStack(to.getBucket()), false);
                                }
                            }
                        }
                    }

                    return ItemInteractionResult.SUCCESS;
                }
                IFluidHandlerItem fluidCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
                if(fluidCap == null) {
                    return ItemInteractionResult.SUCCESS;
                }
                FluidStack inhandFluid = fluidCap.getFluidInTank(0);
                if(inhandFluid == null || inhandFluid.isEmpty()) {
                    int amount = barrel.fill(inhandFluid, IFluidHandlerItem.FluidAction.EXECUTE);
                    if(amount > 0) {
                        fluidCap.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            } else {
                FluidStack fluid = FluidStack.EMPTY;
                fluid = be.fluidTank.getFluidInTank(0);
                int storage = BarrelBlocks.all().get(code()).getCapacity();
                if(fluid == null || fluid.isEmpty()) {
                    player.sendSystemMessage(__("tooltip.nc.liquid_empty", formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
                } else {
                    player.sendSystemMessage(__("tooltip.nc.liquid_stored", fluid.getDisplayName(), formatLiquid(fluid.getAmount()), formatLiquid(storage*1000)).withStyle(ChatFormatting.BLUE));
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = NCLevels.getExistingBlockEntity(pLevel, pPos);

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
        return NCNames.of(asItem());
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof BarrelBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof BarrelBE tile) {
                tile.tickServer();
            }
        };

    }
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (NCItemStacks.hasCustomData(stack)) {
            BarrelBE tileEntity = (BarrelBE) NCLevels.getExistingBlockEntity(world, pos);
            CompoundTag nbtData = NCItemStacks.getTag(stack);
            tileEntity.loadCustomOnly(nbtData, world.registryAccess());
        }
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        BarrelBE BarrelBE = (BarrelBE) pBlockEntity;
        CompoundTag data = BarrelBE.getUpdateTag(pLevel.registryAccess());

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
        int storage = BarrelBlocks.all().get(code()).config().getCapacity() * 1000;

        list.add(__("tooltip.nc.liquid_capacity", formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
        list.add(__("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
    }

    public boolean registered() {
        return BarrelBlocks.registered().containsKey(code());
    }
}
