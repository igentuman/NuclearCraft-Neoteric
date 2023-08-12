package igentuman.nc.radiation.client;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.radiation.data.WorldRadiation.pack;
import static igentuman.nc.radiation.data.WorldRadiation.unpackX;

public class ClientWorldRadiationData {

    public static Map<Long, Long> radiationData = new HashMap<>();
    public static int currentRadiation = 0;
    public static void set(Map<Long, Long> radiation) {
        for(long id: radiation.keySet()) {
            if(radiationData.containsKey(id)) {
                radiationData.replace(id, radiation.get(id));
            } else {
                radiationData.put(id, radiation.get(id));
            }
        }
    }

    public static int getCurrentRadiation() {
        return currentRadiation;
    }

    public static void setCurrentChunk(int x, int z) {
        long id = pack(x, z);
        if(radiationData.containsKey(id)) {
            currentRadiation = unpackX(radiationData.get(id));
        } else {
            currentRadiation = 0;
        }
    }
}