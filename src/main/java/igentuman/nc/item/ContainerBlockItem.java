package igentuman.nc.item;

import igentuman.nc.container.StorageContainerItemContainer;
import igentuman.nc.content.storage.ContainerBlocks;
import igentuman.nc.handler.storage.ContainerInventoryStore;
import igentuman.nc.handler.storage.StoredInventory;
import igentuman.nc.util.capability.CapabilityUtils;
import igentuman.nc.util.capability.ItemCapabilityProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

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
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                prepareServer(itemStack, serverLevel);
                UUID uuid = readUuid(itemStack);
                NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __("container.nc.storage");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                        return new StorageContainerItemContainer<>(containerId, player.blockPosition(), playerInventory, slot, uuid);
                    }
                }, buf -> {
                    buf.writeBlockPos(player.blockPosition());
                    buf.writeInt(slot);
                    buf.writeUUID(uuid);
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
		return asItem().toString();
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(__("tooltip.nc.content_saved").withStyle(ChatFormatting.GRAY));
		list.add(__("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
	}

    public IItemHandler getInventory(ItemStack stack) {
        return (IItemHandler) CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ITEM_HANDLER);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemCapabilityProvider(stack, getInventorySize());
    }

    private int getInventorySize() {
        return getRows()*getColls();
    }

    public int getRows() {
        return ContainerBlocks.all().get(code()).getRows();
    }

    public int getColls() {
        return ContainerBlocks.all().get(code()).getColls();
    }

    /** Reads the stored UUID; may be {@code null} (e.g. a never-touched item, or client before sync). */
    public UUID getUUID(ItemStack stack) {
        return readUuid(stack);
    }

    /** Read-only UUID lookup. Never assigns — safe on the client. */
    public static UUID readUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.hasUUID("uuid")) ? tag.getUUID("uuid") : null;
    }

    /** Server-only: assigns a fresh UUID when missing. Clients must never call this. */
    public static UUID assignUuid(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID("uuid")) tag.putUUID("uuid", UUID.randomUUID());
        return tag.getUUID("uuid");
    }

    /** Writes a server-authoritative UUID trusted from the network (does not generate one). */
    public static void writeUuid(ItemStack stack, UUID uuid) {
        if (uuid != null) stack.getOrCreateTag().putUUID("uuid", uuid);
    }

    /**
     * Server-side entry point: ensures the item has a UUID and migrates any legacy in-NBT
     * {@code "Inventory"} into the store exactly once. Idempotent.
     */
    public void prepareServer(ItemStack stack, ServerLevel level) {
        assignUuid(stack);
        migrateLegacy(stack, level.getServer());
    }

    private void migrateLegacy(ItemStack stack, MinecraftServer server) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Inventory")) return;
        UUID uuid = readUuid(stack);
        if (uuid == null) return;
        ContainerInventoryStore store = ContainerInventoryStore.get(server);
        if (!store.has(uuid)) {
            StoredInventory inv = store.getOrCreate(uuid, getInventorySize());
            inv.read(tag.getCompound("Inventory"));
            store.markDirtyOnly();
        }
        tag.remove("Inventory");
    }

    public String getTier() {
        return code();
    }

    public void toggleMagnetMode(ItemStack stack) {
        if(!stack.getOrCreateTag().contains("magnet")) {
            stack.getOrCreateTag().putBoolean("magnet", true);
        } else {
            boolean mode = stack.getOrCreateTag().getBoolean("magnet");
            stack.getOrCreateTag().putBoolean("magnet", !mode);
        }
    }

    public boolean isMagnetModeEnabled(ItemStack stack) {
        if(!stack.getOrCreateTag().contains("magnet")) {
            stack.getOrCreateTag().putBoolean("magnet", false);
            return false;
        }
        return stack.getOrCreateTag().getBoolean("magnet");
    }
}
