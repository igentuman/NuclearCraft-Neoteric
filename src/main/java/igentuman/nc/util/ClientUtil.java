package igentuman.nc.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class ClientUtil {
    @Nullable
    public static Level tryGetClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static Player tryGetClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static final Pattern TRANSPARENT_BLOCKS = Pattern.compile(".*glass|.*cell.*|photon.*|.*stabilizer.*");

}
