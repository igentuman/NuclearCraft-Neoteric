package igentuman.nc.item;

import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCNames;
import igentuman.nc.container.StorageContainerItemContainer;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;


import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

import static igentuman.nc.util.TextUtils.__;

public class ContainerBlockItem extends BlockItem
{
	public ContainerBlockItem(Block pBlock, Properties props)
	{
		super(pBlock, new Properties().stacksTo(1));
	}

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;

        if (!player.isSteppingCarefully()) {
            if (!level.isClientSide) {
                ((ServerPlayer) player).openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __("container.nc.storage");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                        return new StorageContainerItemContainer<>(containerId, player.blockPosition(), playerInventory, slot);
                    }
                }, buf -> {
                    buf.writeBlockPos(player.blockPosition());
                    buf.writeInt(slot);
                });
            }
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.success(itemStack);
    }
	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return false;
	}

	public String code()
	{
		return NCNames.of(asItem());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> list, TooltipFlag flag)
	{
		list.add(__("tooltip.nc.content_saved").withStyle(ChatFormatting.GRAY));
		list.add(__("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
	}

    public IItemHandler getInventory(ItemStack stack) {
        return stack.getCapability(Capabilities.ItemHandler.ITEM);
    }

    public int getInventorySize() {
        return getRows()*getColls();
    }

    public int getRows() {
        return ContainerBlocks.all().get(code()).getRows();
    }

    public int getColls() {
        return ContainerBlocks.all().get(code()).getColls();
    }

    public UUID getUUID(ItemStack stack) {
        try {
            if(!NCItemStacks.contains(stack, "uuid")) {
                NCItemStacks.putUUID(stack, "uuid", UUID.randomUUID());
            }
            return NCItemStacks.getUUID(stack, "uuid");
        } catch(Exception e) {
            return null;
        }
    }

    public String getTier() {
        return code();
    }

    public void toggleMagnetMode(ItemStack stack) {
        if(!NCItemStacks.contains(stack, "magnet")) {
            NCItemStacks.putBoolean(stack, "magnet", true);
        } else {
            boolean mode = NCItemStacks.getBoolean(stack, "magnet");
            NCItemStacks.putBoolean(stack, "magnet", !mode);
        }
    }

    public boolean isMagnetModeEnabled(ItemStack stack) {
        if(!NCItemStacks.contains(stack, "magnet")) {
            NCItemStacks.putBoolean(stack, "magnet", false);
            return false;
        }
        return NCItemStacks.getBoolean(stack, "magnet");
    }
}
