package igentuman.nc.handler.storage;

import igentuman.nc.NuclearCraft;
import igentuman.nc.network.toClient.PacketSyncContainerInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Access-scoped, single-entry sync. A container's contents reach a client only while it has an open
 * view that renders those contents as a data source (the crafter terminal). Direct block/item GUIs
 * use vanilla container-slot sync and never touch this dispatcher.
 *
 * <p>A mutation on a UUID with no subscribers sends no packet — only a disk {@code setDirty()}.
 */
public class ContainerSyncDispatcher {

    private static final Map<UUID, Set<ServerPlayer>> SUBSCRIBERS = new HashMap<>();

    public static void subscribe(ServerPlayer player, UUID uuid) {
        if (uuid == null) return;
        SUBSCRIBERS.computeIfAbsent(uuid, k -> new HashSet<>()).add(player);
    }

    public static void unsubscribe(ServerPlayer player, UUID uuid) {
        if (uuid == null) return;
        Set<ServerPlayer> subs = SUBSCRIBERS.get(uuid);
        if (subs == null) return;
        subs.remove(player);
        if (subs.isEmpty()) SUBSCRIBERS.remove(uuid);
    }

    public static void unsubscribeAll(ServerPlayer player) {
        SUBSCRIBERS.values().forEach(s -> s.remove(player));
        SUBSCRIBERS.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    public static void sendSnapshot(ServerPlayer player, UUID uuid) {
        if (uuid == null) return;
        StoredInventory inv = ContainerInventoryStore.get(player.getServer()).get(uuid);
        int size = inv != null ? inv.size() : 0;
        NuclearCraft.packetHandler().sendTo(new PacketSyncContainerInventory(uuid, size, inv), player);
    }

    public static void notifyChange(UUID uuid) {
        Set<ServerPlayer> subs = SUBSCRIBERS.get(uuid);
        if (subs == null || subs.isEmpty()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        StoredInventory inv = ContainerInventoryStore.get(server).get(uuid);
        int size = inv != null ? inv.size() : 0;
        PacketSyncContainerInventory pkt = new PacketSyncContainerInventory(uuid, size, inv);
        for (ServerPlayer p : new HashSet<>(subs)) {
            NuclearCraft.packetHandler().sendTo(pkt, p);
        }
    }
}
