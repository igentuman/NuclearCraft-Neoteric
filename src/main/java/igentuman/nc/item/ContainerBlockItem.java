package igentuman.nc.item;

import igentuman.nc.block.storage.ContainerBlock;
import igentuman.nc.container.StorageContainerItemMenu;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class ContainerBlockItem extends BlockItem {

    public ContainerBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        int heldSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : 40;
        UUID uuid = assignUuid(stack);
        String tier = storageName();
        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.nuclearcraft." + tier);
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new StorageContainerItemMenu(containerId, playerInventory, heldSlot, uuid, tier);
            }
        }, buf -> {
            buf.writeVarInt(heldSlot);
            buf.writeUUID(uuid);
            buf.writeUtf(tier);
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.nuclearcraft.content_saved").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.nuclearcraft.use_wrench").withStyle(ChatFormatting.YELLOW));
        boolean magnet = isMagnetEnabled(stack);
        tooltip.accept(Component.translatable(magnet
                        ? "tooltip.nuclearcraft.magnet.on"
                        : "tooltip.nuclearcraft.magnet.off")
                .withStyle(magnet ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
    }

    public String storageName() {
        return getBlock() instanceof ContainerBlock container ? container.storageName() : getBlock().toString();
    }

    private BlockEntityType<?> blockEntityType() {
        return ModEntries.get(storageName()).blockEntity().get();
    }

    @Nullable
    public static UUID readUuid(ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return null;
        return data.copyTagWithoutId().read("uuid", UUIDUtil.CODEC).orElse(null);
    }

    public UUID assignUuid(ItemStack stack) {
        UUID uuid = readUuid(stack);
        if (uuid != null) return uuid;
        uuid = UUID.randomUUID();
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTagWithoutId();
        tag.store("uuid", UUIDUtil.CODEC, uuid);
        stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(blockEntityType(), tag));
        return uuid;
    }

    public boolean isMagnetEnabled(ItemStack stack) {
        return stack.getOrDefault(Registers.CONTAINER_MAGNET.get(), false);
    }

    public void toggleMagnet(ItemStack stack) {
        stack.set(Registers.CONTAINER_MAGNET.get(), !isMagnetEnabled(stack));
    }
}
