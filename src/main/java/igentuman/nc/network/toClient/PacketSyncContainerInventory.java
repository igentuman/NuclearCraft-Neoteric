package igentuman.nc.network.toClient;

import igentuman.nc.client.storage.ClientContainerInventory;
import igentuman.nc.handler.storage.StoredInventory;
import igentuman.nc.network.INcPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

/**
 * Server → client sync of exactly one container inventory. Always single-UUID; never a bulk store dump.
 * Sent to a client only while it has an open view subscribed via {@code ContainerSyncDispatcher}.
 */
public class PacketSyncContainerInventory implements INcPacket {

    private final UUID uuid;
    private final int size;
    private final StoredInventory inventory; // null on send ⇒ empty entry

    public PacketSyncContainerInventory(UUID uuid, int size, StoredInventory inventory) {
        this.uuid = uuid;
        this.size = size;
        this.inventory = inventory;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> ClientContainerInventory.put(uuid, inventory));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeVarInt(size);
        int count = 0;
        if (inventory != null) {
            for (int i = 0; i < size; i++) {
                if (!inventory.get(i).isEmpty()) count++;
            }
        }
        buffer.writeVarInt(count);
        if (inventory == null) return;
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.get(i);
            if (stack.isEmpty()) continue;
            buffer.writeVarInt(i);
            ItemStack one = stack.copy();
            one.setCount(1);
            buffer.writeItem(one);
            buffer.writeVarInt(stack.getCount());
        }
    }

    public static PacketSyncContainerInventory decode(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        int size = buffer.readVarInt();
        StoredInventory inv = new StoredInventory(size);
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            int slot = buffer.readVarInt();
            ItemStack stack = buffer.readItem();
            stack.setCount(buffer.readVarInt());
            inv.set(slot, stack);
        }
        return new PacketSyncContainerInventory(uuid, size, inv);
    }
}
