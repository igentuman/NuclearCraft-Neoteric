package igentuman.nc.setup.energy;

import igentuman.nc.block.entity.energy.BatteryBE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BatteryBlocks {

    private static HashMap<String, BatteryBlockPrefab> all = new HashMap<>();
    private static HashMap<String, BatteryBlockPrefab> registered = new HashMap<>();

    public static HashMap<String, BatteryBlockPrefab> all() {
        if(all.isEmpty()) {
            all.put("basic_voltaic_pile", new BatteryBlockPrefab("basic_voltaic_pile",1_600_000));
            all.put("advanced_voltaic_pile", new BatteryBlockPrefab("advanced_voltaic_pile",6_400_000));
            all.put("du_voltaic_pile", new BatteryBlockPrefab("du_voltaic_pile",25_600_000));
            all.put("elite_voltaic_pile", new BatteryBlockPrefab("elite_voltaic_pile",102_400_000));

            all.put("basic_lithium_ion_battery", new BatteryBlockPrefab("basic_lithium_ion_battery",32_000_000));
            all.put("advanced_lithium_ion_battery", new BatteryBlockPrefab("advanced_lithium_ion_battery",128_000_000));
            all.put("du_lithium_ion_battery", new BatteryBlockPrefab("du_lithium_ion_battery",512_000_000));
            all.put("elite_lithium_ion_battery", new BatteryBlockPrefab("elite_lithium_ion_battery", 2_048_000_000));
        }
        return all;
    }

    public static HashMap<String, BatteryBlockPrefab> registered() {
        if(registered.isEmpty()) {
            for(String name: all().keySet()) {
                if (all().get(name).config().isRegistered())
                    registered.put(name,all().get(name));
            }
        }
        return registered;
    }

    public static List<Boolean> initialRegistered() {
        List<Boolean> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(true);
        }
        return tmp;
    }

    public static List<Integer> initialPower() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getStorage());
        }
        return tmp;
    }

}
