package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public record PacketCrafterExtract(BlockPos pos, ItemStack sample, boolean single, boolean toInventory)
        implements CustomPacketPayload {

    public static final Type<PacketCrafterExtract> TYPE = new Type<>(NuclearCraft.rl("crafter_extract"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCrafterExtract> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketCrafterExtract::pos,
                    ItemStack.OPTIONAL_STREAM_CODEC, PacketCrafterExtract::sample,
                    ByteBufCodecs.BOOL, PacketCrafterExtract::single,
                    ByteBufCodecs.BOOL, PacketCrafterExtract::toInventory,
                    PacketCrafterExtract::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCrafterExtract packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;
            if (packet.sample() == null || packet.sample().isEmpty()) return;

            EngineersCrafterBE be = menu.blockEntity;
            var storage = be.getItemHandler(null);
            var resource = ItemResource.of(packet.sample());
            int amount = packet.single() ? 1 : packet.sample().getMaxStackSize();
            try (var transaction = Transaction.openRoot()) {
                if (packet.toInventory()) {
                    int moved = ResourceHandlerUtil.move(storage, PlayerInventoryWrapper.of(player),
                            resource::equals, amount, transaction);
                    if (moved == 0) return;
                } else {
                    int extracted = storage.extract(resource, amount, transaction);
                    if (extracted == 0 || CarriedSlotWrapper.of(menu).insert(resource, extracted, transaction) != extracted) return;
                }
                transaction.commit();
            }

            be.markUpdated();
            menu.broadcastChanges();
        });
    }
}
