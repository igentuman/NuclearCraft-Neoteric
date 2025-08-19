package igentuman.nc.radiation.client;

import igentuman.nc.client.NcClient;
import igentuman.nc.radiation.data.WorldRadiation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;

import static igentuman.nc.client.NcClient.tryGetClientWorld;

public class ClientRadiationData {

    protected static int currentRadiation = 0;
    protected static HashMap<ResourceKey<Level>,WorldRadiation> worldRadiation = new HashMap<>();
    protected static long playerRadiation = 0;

    public static void setWorldRadiation(HashMap<Long, Long> radiation) {
        worldRadiation.remove(NcClient.tryGetClientWorld().dimension());
        worldRadiation.put(NcClient.tryGetClientWorld().dimension(), new WorldRadiation(radiation));
    }

    public static int radiationStage() {
        if (getCurrentWorldRadiation() < 50000) return 0;
        if (getCurrentWorldRadiation() < 250000) return 1;
        if (getCurrentWorldRadiation() < 750000) return 2;
        if (getCurrentWorldRadiation() < 1250000) return 3;
        if (getCurrentWorldRadiation() < 2000000) return 4;
        if (getCurrentWorldRadiation() < 5000000) return 5;
        return 6;
    }

    public static int getCurrentWorldRadiation() {
        return Math.max(0, currentRadiation);
    }

    public static void setCurrentChunk(int x, int z, Level level) {
        if (!worldRadiation.containsKey(level.dimension())) {
            worldRadiation.put(level.dimension(), new WorldRadiation());
        }
        currentRadiation = worldRadiation.get(level.dimension()).getChunkRadiation(x, z);
    }

    public static void setPlayerRadiation(long radiation) {
        playerRadiation = radiation;
    }

    public static long getPlayerRadiation() {
        return playerRadiation;
    }
}