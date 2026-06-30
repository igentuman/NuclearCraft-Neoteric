package igentuman.nc.multiblock;

import igentuman.nc.registration.ModEntry;

import java.util.HashMap;
import java.util.Map;

public class MultiblockRegistry {
    public static final Map<String, MultiblockEntry> ENTRIES = new HashMap<>();

    public static void register(MultiblockEntry entry) {
        ENTRIES.put(entry.name(), entry);
    }

    public static MultiblockEntry get(String name) {
        return ENTRIES.get(name);
    }

    public static MultiblockEntry getByController(String name) {
        for (MultiblockEntry entry : ENTRIES.values()) {
            ModEntry controller = entry.controllerEntry();
            if (controller != null && controller.name().equals(name)) return entry;
        }
        return null;
    }

    public static MultiblockEntry getByPort(String name) {
        for (MultiblockEntry entry : ENTRIES.values()) {
            for (ModEntry port : entry.portEntries()) {
                if (port.name().equals(name)) return entry;
            }
        }
        return null;
    }
}
