package igentuman.nc.block;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.BarrelBE;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.network.toServer.StorageSideConfig;
import igentuman.nc.setup.registration.NCStorageBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;


import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static igentuman.nc.setup.registration.NCItems.MULTITOOL;

public class BarrelBlock extends Block {

    public BarrelBlock(Properties pProperties) {
        super(pProperties);
    }

   /* @Override
    public InteractionResult use(BlockState state, World level, BlockPos pos, PlayerEntity player, InteractionHand hand, BlockHitResult result) {
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
                    player.sendMessage(new TranslationTextComponent("tooltip.nc.liquid_empty", formatLiquid(storage)).withStyle(TextFormatting.BLUE), UUID.randomUUID());
                } else {
                    player.sendMessage(new TranslationTextComponent("tooltip.nc.liquid_stored", fluid.getDisplayName(), formatLiquid(fluid.getAmount()), formatLiquid(storage)).withStyle(TextFormatting.BLUE), UUID.randomUUID());
                }
            }
        }
        return InteractionResult.SUCCESS;
    }*/

/*    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NCStorageBlocks.STORAGE_BE.get(code()).get().create(pPos, pState);
    }*/

    public String code()
    {
        return asItem().toString();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (stack.hasTag()) {
            BarrelBE tileEntity = (BarrelBE) world.getBlockEntity(pos);
            CompoundNBT nbtData = stack.getTag();
            tileEntity.load(state, nbtData);
        }
    }

/*
    @Override
    public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        BarrelBE BarrelBE = (BarrelBE) pBlockEntity;
        CompoundNBT data = BarrelBE.getUpdateTag();

        ItemStack drop = new ItemStack(this);
        drop.setTag(data);
        if (!pLevel.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), drop);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
    }
*/


/*    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable BlockGetter world, List<TextComponent> list, TooltipFlag flag)
    {
        int storage = BarrelBlocks.all().get(code()).config().getCapacity();

        list.add(new TranslationTextComponent("tooltip.nc.liquid_capacity", formatLiquid(storage)).withStyle(TextFormatting.BLUE));
        list.add(new TranslationTextComponent("tooltip.nc.use_multitool").withStyle(TextFormatting.YELLOW));
    }*/

    public String formatLiquid(int val)
    {
        return TextUtils.numberFormat(val/1000)+" B";
    }

}
