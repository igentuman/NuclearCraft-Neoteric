package igentuman.nc.setup.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BarrelBlocks {

    private static HashMap<String, BarrelBlockPrefab> all = new HashMap<>();
    private static HashMap<String, BarrelBlockPrefab> registered = new HashMap<>();

    public static HashMap<String, BarrelBlockPrefab> all() {
        if (all.isEmpty()) {
            all.put("basic_barrel", new BarrelBlockPrefab("basic_barrel", 128));
            all.put("advanced_barrel", new BarrelBlockPrefab("advanced_barrel", 512));
            all.put("du_barrel", new BarrelBlockPrefab("du_barrel", 2048));
            all.put("elite_barrel", new BarrelBlockPrefab("elite_barrel", 8192));
        }
        return all;
    }

    public static HashMap<String, BarrelBlockPrefab> registered() {
        if (registered.isEmpty()) {
            for (String name : all().keySet()) {
                if (all().get(name).config().isRegistered())
                    registered.put(name, all().get(name));
            }
        }
        return registered;
    }

    public static List<Boolean> initialRegistered() {
        List<Boolean> tmp = new ArrayList<>();
        for (String name : all().keySet()) {
            tmp.add(true);
        }
        return tmp;
    }

    public static List<Integer> initialCapacity() {
        List<Integer> tmp = new ArrayList<>();
        for (String name : all().keySet()) {
            tmp.add(all().get(name).getCapacity());
        }
        return tmp;
    }
}
