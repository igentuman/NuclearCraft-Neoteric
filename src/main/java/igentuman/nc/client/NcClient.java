package igentuman.nc.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

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
    public static World tryGetClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static PlayerEntity tryGetClientPlayer() {
        return Minecraft.getInstance().player;
    }
}