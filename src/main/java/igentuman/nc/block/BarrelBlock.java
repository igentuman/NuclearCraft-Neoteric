package igentuman.nc.block;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.BarrelBE;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.network.toServer.StorageSideConfig;
import igentuman.nc.setup.registration.NCStorageBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static igentuman.nc.setup.registration.NCItems.MULTITOOL;
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            BarrelBE be = (BarrelBE)level.getBlockEntity(pos);
            ItemStack handStack = player.getItemInHand(hand);
            IFluidHandler barrel = be.getFluidHandler().orElse(null);
            if(handStack.getItem().equals(MULTITOOL.get())) {
                Direction dirToChange = result.getDirection();
                if(player.isShiftKeyDown()) {
                    dirToChange = dirToChange.getOpposite();
                }
                NuclearCraft.packetHandler().sendToServer(new StorageSideConfig(pos, dirToChange.ordinal()));
            } else
            if(!handStack.equals(ItemStack.EMPTY)) {
                if(handStack.getItem() instanceof BucketItem) {
                    Fluid to = ((BucketItem) handStack.getItem()).getFluid();
                    if(to == null || to == FluidStack.EMPTY.getFluid()) {
                        if(!barrel.getFluidInTank(0).isEmpty() && barrel.getFluidInTank(0).getAmount() >= 1000) {
                            ItemStack bucket = new ItemStack(barrel.getFluidInTank(0).getFluid().getBucket());
                            barrel.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                            if(handStack.getCount() == 1) {
                                player.setItemInHand(hand, bucket);
                            } else {
                                handStack.shrink(1);
                                if(!player.getInventory().add(bucket)) {
                                    player.drop(bucket, false);
                                }
                            }
                        }
                    } else {
                        int filled = barrel.fill(new FluidStack(to, 1000), IFluidHandler.FluidAction.SIMULATE);
                        if(filled == 1000) {
                            barrel.fill(new FluidStack(to, 1000), IFluidHandler.FluidAction.EXECUTE);
                            if(player.isCreative()) return InteractionResult.SUCCESS;
                            if(handStack.getCount() == 1) {
                                player.setItemInHand(hand, new ItemStack(BUCKET));
                            } else {
                                handStack.shrink(1);
                                if(!player.getInventory().add(new ItemStack(to.getBucket()))) {
                                    player.drop(new ItemStack(to.getBucket()), false);
                                }
                            }
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
                IFluidHandlerItem fluidCap = handStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
                if(fluidCap == null) {
                    return InteractionResult.SUCCESS;
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
                fluid = be.getFluidHandler().orElseGet(null).getFluidInTank(0);
                int storage = BarrelBlocks.all().get(code()).getCapacity();
                if(fluid == null || fluid.isEmpty()) {
                    player.sendMessage(new TranslatableComponent("tooltip.nc.liquid_empty", formatLiquid(storage)).withStyle(ChatFormatting.BLUE), UUID.randomUUID());
                } else {
                    player.sendMessage(new TranslatableComponent("tooltip.nc.liquid_stored", fluid.getDisplayName(), formatLiquid(fluid.getAmount()), formatLiquid(storage)).withStyle(ChatFormatting.BLUE), UUID.randomUUID());
                }
            }
        }
        return InteractionResult.SUCCESS;
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

        if (stack.hasTag()) {
            BarrelBE tileEntity = (BarrelBE) world.getBlockEntity(pos);
            CompoundTag nbtData = stack.getTag();
            tileEntity.load(nbtData);
        }
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        BarrelBE BarrelBE = (BarrelBE) pBlockEntity;
        CompoundTag data = BarrelBE.getUpdateTag();

        ItemStack drop = new ItemStack(this);
        drop.setTag(data);
        if (!pLevel.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), drop);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable BlockGetter world, List<Component> list, TooltipFlag flag)
    {
        int storage = BarrelBlocks.all().get(code()).config().getCapacity();

        list.add(new TranslatableComponent("tooltip.nc.liquid_capacity", formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
        list.add(new TranslatableComponent("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
    }

    public String formatLiquid(int val)
    {
        return TextUtils.numberFormat(val/1000)+" B";
    }

}
