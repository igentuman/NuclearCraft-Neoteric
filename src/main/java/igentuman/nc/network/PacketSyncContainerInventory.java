package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.storage.ClientContainerInventory;
import igentuman.nc.handler.storage.StoredInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PacketSyncContainerInventory(UUID uuid, int size, StoredInventory inventory)
        implements CustomPacketPayload {

    public static final Type<PacketSyncContainerInventory> TYPE =
            new Type<>(NuclearCraft.rl("sync_container_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncContainerInventory> STREAM_CODEC =
            StreamCodec.of(PacketSyncContainerInventory::encode, PacketSyncContainerInventory::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, PacketSyncContainerInventory pkt) {
        buf.writeUUID(pkt.uuid);
        buf.writeVarInt(pkt.size);
        int count = 0;
        if (pkt.inventory != null) {
            for (int i = 0; i < pkt.size; i++) {
                if (!pkt.inventory.get(i).isEmpty()) count++;
            }
        }
        buf.writeVarInt(count);
        if (pkt.inventory == null) return;
        for (int i = 0; i < pkt.size; i++) {
            ItemStack stack = pkt.inventory.get(i);
            if (stack.isEmpty()) continue;
            buf.writeVarInt(i);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    }

    private static PacketSyncContainerInventory decode(RegistryFriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        int size = buf.readVarInt();
        StoredInventory inv = new StoredInventory(size);
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            int slot = buf.readVarInt();
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            inv.set(slot, stack);
        }
        return new PacketSyncContainerInventory(uuid, size, inv);
    }

    public static void handle(PacketSyncContainerInventory packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientContainerInventory.put(packet.uuid(), packet.inventory()));
    }
}
