package igentuman.nc.api.catalyst;

import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Global registry holding every registered {@link CatalystDef} keyed by {@link CatalystType}. */
public final class CatalystRegistry {

    private static final Map<CatalystType, List<CatalystDef>> ENTRIES = new HashMap<>();

    private CatalystRegistry() {}

    /**
     * Registers an item binding during startup. Addons call this from
     * {@link NewRegistryEvent} on both sides with an item from their own deferred register.
     */
    public static void register(CatalystDef def) {
        List<CatalystDef> definitions = new ArrayList<>(byType(def.type()));
        definitions.add(def);
        ENTRIES.put(def.type(), List.copyOf(definitions));
    }

    public static List<CatalystDef> byType(CatalystType type) {
        return ENTRIES.getOrDefault(type, List.of());
    }
}
