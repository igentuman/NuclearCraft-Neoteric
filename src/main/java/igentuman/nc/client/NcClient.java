package igentuman.nc.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import java.util.Map;
import java.util.UUID;

public class NcClient {

    private NcClient() {
    }

    public static final Map<UUID, String> clientUUIDMap = new Object2ObjectOpenHashMap<>();
    public static boolean renderHUD = true;

    public static long ticksPassed = 0;

    @Nullable
    public static Level tryGetClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static Player tryGetClientPlayer() {
        return Minecraft.getInstance().player;
    }
}