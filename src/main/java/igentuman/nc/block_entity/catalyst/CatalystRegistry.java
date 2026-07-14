package igentuman.nc.block_entity.catalyst;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Global registry holding every registered {@link CatalystDef} keyed by {@link CatalystType}. */
public class CatalystRegistry {

    public static final Map<CatalystType, List<CatalystDef>> ENTRIES = new EnumMap<>(CatalystType.class);

    public static void register(CatalystDef def) {
        ENTRIES.computeIfAbsent(def.type(), t -> new ArrayList<>()).add(def);
    }

    public static List<CatalystDef> byType(CatalystType type) {
        return ENTRIES.getOrDefault(type, List.of());
    }
}
