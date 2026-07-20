package igentuman.nc.block.storage;

import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.BatteryBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import static igentuman.nc.util.TextUtils.formatEnergy;

public class BatteryBlock extends AbstractStorageBlock {

    public BatteryBlock(Properties properties, String name) {
        super(properties, name);
    }

    @Override
    protected AbstractStorageBlock create(Properties properties) {
        return new BatteryBlock(properties, name);
    }

    @Override
    protected boolean persistOnDrop(AbstractStorageBE be) {
        return be instanceof BatteryBE battery && battery.hasContent();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (tryWrench(stack, level, pos, player, hit)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BatteryBE battery) {
            player.sendSystemMessage(Component.translatable("tooltip.nuclearcraft.energy_stored",
                    formatEnergy(battery.energyStorage.getEnergyStored()),
                    formatEnergy(battery.energyStorage.getMaxEnergyStored())).withStyle(ChatFormatting.BLUE));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
