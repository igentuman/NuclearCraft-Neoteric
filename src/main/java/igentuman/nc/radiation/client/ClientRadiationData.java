package igentuman.nc.radiation.client;

import igentuman.nc.radiation.data.WorldRadiation;

import java.util.HashMap;

import static igentuman.nc.client.NcClient.tryGetClientWorld;

public class ClientRadiationData {

    protected static int currentRadiation = 0;
    protected static WorldRadiation worldRadiation = new WorldRadiation();
    protected static long playerRadiation = 0;

    public static void setWorldRadiation(HashMap<Long, Long> radiation) {
        worldRadiation.chunkRadiation = radiation;
        worldRadiation.level = tryGetClientWorld();
    }

    public static int getCurrentWorldRadiation() {
        return Math.max(0, currentRadiation);
    }

    public static void setCurrentChunk(int x, int z) {
        currentRadiation = worldRadiation.getChunkRadiation(x, z);
    }

    public static void setPlayerRadiation(long radiation) {
        playerRadiation = radiation;
    }

    public static long getPlayerRadiation() {
        return playerRadiation;
    }
}