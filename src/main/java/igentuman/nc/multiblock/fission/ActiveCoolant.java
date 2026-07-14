package igentuman.nc.multiblock.fission;

/** Enumerates active heat-sink coolant types, mapping each to its source fluid, cooling value, and fluid tank index. */
public enum ActiveCoolant {
    WATER("water", 250),
    REDSTONE("redstone", 270),
    CRYOTHEUM("cryotheum", 480),
    ENDERIUM("enderium", 360),
    LIQUID_NITROGEN("liquid_nitrogen", 555),
    LIQUID_HELIUM("liquid_helium", 420);

    public static final ActiveCoolant[] VALUES = values();
    public static final int COUNT = VALUES.length;

    public static final int BOILING_INPUT_TANK = 0;
    public static final int STEAM_OUTPUT_TANK = 1 + COUNT;
    public static final int TOTAL_TANKS = 2 + COUNT;

    public final String coolant;
    public final double sinkHeat;

    ActiveCoolant(String coolant, double sinkHeat) {
        this.coolant = coolant;
        this.sinkHeat = sinkHeat;
    }

    public String sinkName() {
        return "active_" + coolant;
    }

    public int tankIndex() {
        return 1 + ordinal();
    }

    public static ActiveCoolant bySinkName(String sinkName) {
        for (ActiveCoolant c : VALUES) {
            if (c.sinkName().equals(sinkName)) return c;
        }
        return null;
    }
}
