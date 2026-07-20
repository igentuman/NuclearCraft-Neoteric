package igentuman.nc.block.storage;

import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.BarrelBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import static igentuman.nc.util.TextUtils.formatLiquid;

public class BarrelBlock extends AbstractStorageBlock {

    public BarrelBlock(Properties properties, String name) {
        super(properties, name);
    }

    @Override
    protected AbstractStorageBlock create(Properties properties) {
        return new BarrelBlock(properties, name);
    }

    @Override
    protected boolean persistOnDrop(AbstractStorageBE be) {
        return be instanceof BarrelBE barrel && barrel.hasContent();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (tryWrench(stack, level, pos, player, hit)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BarrelBE barrel) {
            if (!level.isClientSide) {
                boolean handled = FluidUtil.interactWithFluidHandler(player, hand, barrel.fluidTank);
                if (!handled) sendInfo(player, barrel);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BarrelBE barrel) {
            sendInfo(player, barrel);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void sendInfo(Player player, BarrelBE barrel) {
        FluidStack fluid = barrel.fluidTank.getFluidInTank(0);
        int capacity = barrel.getCapacity();
        if (fluid.isEmpty()) {
            player.sendSystemMessage(Component.translatable("tooltip.nuclearcraft.liquid_empty",
                    formatLiquid(capacity)).withStyle(ChatFormatting.BLUE));
        } else {
            player.sendSystemMessage(Component.translatable("tooltip.nuclearcraft.liquid_stored",
                    fluid.getHoverName(), formatLiquid(fluid.getAmount()), formatLiquid(capacity)).withStyle(ChatFormatting.BLUE));
        }
    }
}
