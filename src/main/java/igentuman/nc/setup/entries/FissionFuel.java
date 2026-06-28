package igentuman.nc.setup.entries;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FuelEntry;
import igentuman.nc.setup.ModEntries;

import java.util.function.Consumer;

public class FissionFuel extends ModEntries {

    private static boolean initialized = false;

    public static void fissionFuel() {
        if (initialized) return;
        initialized = true;

        // thorium
        register(new FuelDef("thorium", "tbu", 120, 18, 234, 720, 125).isotopes(230, 232));

        // uranium
        register(new FuelDef("uranium", "heu-233", 1152, 360, 39, 133, 115).isotopes(233, 238));
        register(new FuelDef("uranium", "heu-235", 960, 300, 51, 240, 105).isotopes(235, 238));
        register(new FuelDef("uranium", "leu-233", 288, 60, 78, 133, 110).isotopes(233, 238));
        register(new FuelDef("uranium", "leu-235", 240, 50, 102, 240, 100).isotopes(235, 238));

        // neptunium
        register(new FuelDef("neptunium", "hen-236", 720, 216, 35, 99, 115).isotopes(236, 237));
        register(new FuelDef("neptunium", "len-236", 180, 36, 70, 99, 110).isotopes(236, 237));

        // plutonium
        register(new FuelDef("plutonium", "hep-239", 840, 240, 49, 229, 145).isotopes(239, 242));
        register(new FuelDef("plutonium", "hep-241", 1320, 420, 42, 158, 130).isotopes(241, 242));
        register(new FuelDef("plutonium", "lep-239", 210, 40, 99, 229, 150).isotopes(239, 242));
        register(new FuelDef("plutonium", "lep-241", 330, 70, 84, 158, 125).isotopes(241, 242));

        // americium
        register(new FuelDef("americium", "hea-242", 1536, 564, 32, 92, 140).isotopes(242, 243));
        register(new FuelDef("americium", "lea-242", 384, 94, 65, 74, 135).isotopes(242, 243));

        // curium
        register(new FuelDef("curium", "hecm-243", 1680, 672, 33, 75, 150).isotopes(243, 246));
        register(new FuelDef("curium", "hecm-245", 1296, 408, 37, 121, 155).isotopes(245, 246));
        register(new FuelDef("curium", "hecm-247", 1104, 324, 36, 108, 160).isotopes(247, 246));
        register(new FuelDef("curium", "lecm-243", 420, 112, 66, 75, 145).isotopes(243, 246));
        register(new FuelDef("curium", "lecm-245", 324, 68, 75, 121, 150).isotopes(245, 246));
        register(new FuelDef("curium", "lecm-247", 276, 54, 72, 108, 155).isotopes(247, 246));

        // berkelium
        register(new FuelDef("berkelium", "heb-248", 1080, 312, 32, 92, 170).isotopes(248, 247));
        register(new FuelDef("berkelium", "leb-248", 270, 52, 73, 108, 165).isotopes(248, 247));

        // californium
        register(new FuelDef("californium", "hecf-249", 1728, 696, 30, 53, 180).isotopes(249, 252));
        register(new FuelDef("californium", "hecf-251", 1800, 720, 35, 100, 185).isotopes(251, 252));
        register(new FuelDef("californium", "lecf-249", 432, 116, 60, 53, 175).isotopes(249, 252));
        register(new FuelDef("californium", "lecf-251", 450, 120, 71, 100, 180).isotopes(251, 252));

        // mixed (MOX)
        register(new FuelDef("mixed", "mix-239", 310, 57.5, 94, 218, 105).isotopes(239, 238));
        register(new FuelDef("mixed", "mix-241", 468, 97.5, 80, 151, 115).isotopes(241, 238));

        // xenorium (special: base form only)
        register(new FuelDef("xenorium", "xen-298", 4536, 768, 32, 92, 140).isotopes(298, 298));
    }

    /** Registers a fuel (built-in or KubeJS) and all its item/fluid forms. */
    public static FuelEntry register(FuelDef def) {
        return FuelEntry.register(def);
    }

    /** Disables a fuel by {@code group} and {@code name} (KubeJS removal). */
    public static void remove(String group, String name) {
        FuelEntry entry = ModEntries.FUELS.get(group + "/" + name);
        if (entry != null) {
            entry.disable();
        }
    }

    /** Overrides parameters of an existing fuel in place (KubeJS override). */
    public static void override(String group, String name, Consumer<FuelDef> mutator) {
        FuelEntry entry = ModEntries.FUELS.get(group + "/" + name);
        if (entry != null) {
            entry.override(mutator);
        }
    }
}
