package igentuman.nc.content.fuel;

import com.google.gson.JsonArray;
import igentuman.nc.util.JSONUtil;

import java.util.HashMap;
import java.util.List;

public class FuelManager {
    protected static HashMap<String, HashMap<String, NCFuel>> all;
    protected static HashMap<String, HashMap<String, NCFuel>> registered;
    protected static HashMap<String, Double> heat;
    protected static HashMap<String, Integer> efficiency;
    protected static HashMap<String, Integer> criticality;
    protected static HashMap<String, Integer> depletion;
    protected static HashMap<String, Integer> forge_energy;

    public static HashMap<String, HashMap<String, NCFuel>> all() {
        if (all == null) {
            all = new HashMap<>();
            List<JsonArray> data = JSONUtil.loadAllJsonFromConfig("fission_fuel");
            if(data == null) {
                return all;
            }
            for (JsonArray array : data) {
                for (int i = 0; i < array.size(); i++) {
                    NCFuel fuel = NCFuel.of(array.get(i).getAsJsonObject());
                    if (fuel != null) {
                        if (!all.containsKey(fuel.group)) {
                            all.put(fuel.group, new HashMap<>());
                        }
                        all.get(fuel.group).put(fuel.name, fuel);
                    }
                }
            }
        }
        return all;
    }
}
