package igentuman.nc.handler.storage;

import igentuman.nc.network.PacketSyncContainerInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        PacketDistributor.sendToPlayer(player, new PacketSyncContainerInventory(uuid, size, inv));
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
            PacketDistributor.sendToPlayer(p, pkt);
        }
    }
}
