package igentuman.nc.content.storage;

import java.util.LinkedHashMap;
import java.util.Map;

public class StorageDefs {

    public static final Map<String, Integer> BARRELS = new LinkedHashMap<>();

    public static final Map<String, int[]> CONTAINERS = new LinkedHashMap<>();

    public static final Map<String, int[]> BATTERIES = new LinkedHashMap<>();

    static {
        BARRELS.put("basic_barrel", 128);
        BARRELS.put("advanced_barrel", 512);
        BARRELS.put("du_barrel", 2048);
        BARRELS.put("elite_barrel", 8192);

        CONTAINERS.put("basic_storage_container", new int[]{3, 9});
        CONTAINERS.put("advanced_storage_container", new int[]{6, 9});
        CONTAINERS.put("du_storage_container", new int[]{7, 12});
        CONTAINERS.put("elite_storage_container", new int[]{9, 13});

        BATTERIES.put("basic_voltaic_pile", new int[]{1_600_000, 1});
        BATTERIES.put("advanced_voltaic_pile", new int[]{6_400_000, 2});
        BATTERIES.put("du_voltaic_pile", new int[]{25_600_000, 3});
        BATTERIES.put("elite_voltaic_pile", new int[]{102_400_000, 4});
        BATTERIES.put("basic_lithium_ion_battery", new int[]{32_000_000, 3});
        BATTERIES.put("advanced_lithium_ion_battery", new int[]{128_000_000, 4});
        BATTERIES.put("du_lithium_ion_battery", new int[]{512_000_000, 5});
        BATTERIES.put("elite_lithium_ion_battery", new int[]{2_048_000_000, 6});
    }

    public static int barrelCapacityMb(String name) {
        return BARRELS.getOrDefault(name, 1) * 1000;
    }

    public static int containerRows(String name) {
        return CONTAINERS.getOrDefault(name, new int[]{3, 9})[0];
    }

    public static int containerColumns(String name) {
        return CONTAINERS.getOrDefault(name, new int[]{3, 9})[1];
    }

    public static int containerSize(String name) {
        return containerRows(name) * containerColumns(name);
    }

    public static int batteryStorage(String name) {
        return BATTERIES.getOrDefault(name, new int[]{1, 0})[0];
    }
}
