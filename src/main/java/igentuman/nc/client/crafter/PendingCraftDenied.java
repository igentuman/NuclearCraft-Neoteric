package igentuman.nc.client.crafter;

import igentuman.nc.network.PacketCraftDenied;
import org.jetbrains.annotations.Nullable;

public class PendingCraftDenied {

    private static PacketCraftDenied pending;

    public static void set(PacketCraftDenied result) {
        pending = result;
    }

    @Nullable
    public static PacketCraftDenied take() {
        PacketCraftDenied result = pending;
        pending = null;
        return result;
    }
}
