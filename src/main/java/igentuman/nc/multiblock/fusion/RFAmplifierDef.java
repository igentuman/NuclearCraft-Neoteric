package igentuman.nc.multiblock.fusion;

import java.util.HashMap;
import java.util.Map;

/** Defines fusion RF amplifier tiers and their power, heat, voltage, temperature, and efficiency stats. */
public final class RFAmplifierDef {

    public final String name;
    public final int power;
    public final int heat;
    public final int voltage;
    public final int maxTemp;
    public final int efficiency;

    public RFAmplifierDef(String name, int power, int heat, int voltage, int maxTemp, int efficiency) {
        this.name = name;
        this.power = power;
        this.heat = heat;
        this.voltage = voltage;
        this.maxTemp = maxTemp;
        this.efficiency = efficiency;
    }

    private static final Map<String, RFAmplifierDef> BY_NAME = new HashMap<>();

    static {
        register("basic_rf_amplifier",              500, 300,  200000, 350000, 50);
        register("magnesium_diboride_rf_amplifier", 1000, 580,  500000,  39000, 80);
        register("niobium_tin_rf_amplifier",         2000,1140, 1000000,  18000, 90);
        register("niobium_titanium_rf_amplifier",    4000,2260, 2000000,  10000, 95);
        register("bscco_rf_amplifier",               8000,4500, 4000000, 104000, 99);
    }

    private static void register(String name, int power, int heat, int voltage, int maxTemp, int eff) {
        BY_NAME.put(name, new RFAmplifierDef(name, power, heat, voltage, maxTemp, eff));
    }

    public static RFAmplifierDef get(String blockName) {
        return BY_NAME.get(blockName);
    }

    public static boolean isAmplifier(String blockName) {
        return BY_NAME.containsKey(blockName);
    }
}
