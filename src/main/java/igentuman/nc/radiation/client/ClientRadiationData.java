package igentuman.nc.radiation.client;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.radiation.data.WorldRadiation.pack;
import static igentuman.nc.radiation.data.WorldRadiation.unpackX;

public class ClientRadiationData {

    public static Map<Long, Long> radiationData = new HashMap<>();
    protected static int currentRadiation = 0;
    protected static int playerRadiation = 0;

    public static void setWorldRadiation(Map<Long, Long> radiation) {
        for(long id: radiation.keySet()) {
            if(radiationData.containsKey(id)) {
                radiationData.replace(id, radiation.get(id));
            } else {
                radiationData.put(id, radiation.get(id));
            }
        }
    }

    public static int getCurrentWorldRadiation() {
        return Math.max(0, currentRadiation);
    }

    public static void setCurrentChunk(int x, int z) {
        long id = pack(x, z);
        if(radiationData.containsKey(id)) {
            currentRadiation = unpackX(radiationData.get(id));
        } else {
            currentRadiation = 0;
        }
    }

    public static void setPlayerRadiation(int radiation) {
        playerRadiation = radiation;
    }

    public static long getPlayerRadiation() {
        return playerRadiation;
    }
}