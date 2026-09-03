package igentuman.nc.hub;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FirstUseNotice {

    private static Boolean acknowledged;

    private FirstUseNotice() {}

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("nuclearcraft").resolve("hub_ack.flag");
    }

    public static boolean isAcknowledged() {
        if (acknowledged == null) {
            acknowledged = Files.exists(path());
        }
        return acknowledged;
    }

    public static void acknowledge() {
        acknowledged = true;
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            Files.writeString(p, "1");
        } catch (IOException ignored) {
        }
    }
}
