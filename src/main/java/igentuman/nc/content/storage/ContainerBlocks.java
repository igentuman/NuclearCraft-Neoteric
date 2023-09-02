package igentuman.nc.content.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContainerBlocks {

    private static HashMap<String, ContainerBlockPrefab> all = new HashMap<>();
    private static HashMap<String, ContainerBlockPrefab> registered = new HashMap<>();

    public static HashMap<String, ContainerBlockPrefab> all() {
        if (all.isEmpty()) {
            all.put("basic_storage_container", new ContainerBlockPrefab("basic_storage_container", 3, 9));
            all.put("advanced_storage_container", new ContainerBlockPrefab("advanced_storage_container", 6, 9));
            all.put("du_storage_container", new ContainerBlockPrefab("du_storage_container", 7, 12));
            all.put("elite_storage_container", new ContainerBlockPrefab("elite_storage_container", 9, 13));
        }
        return all;
    }

    public static HashMap<String, ContainerBlockPrefab> registered() {
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

}
