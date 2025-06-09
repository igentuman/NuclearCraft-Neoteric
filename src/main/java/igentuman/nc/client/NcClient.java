package igentuman.nc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NcClient {

    private NcClient() {
    }

    @Nullable
    public static Level tryGetClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static Player tryGetClientPlayer() {
        return Minecraft.getInstance().player;
    }
}