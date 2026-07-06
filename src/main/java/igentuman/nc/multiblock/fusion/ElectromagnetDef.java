package igentuman.nc.multiblock.fusion;

import java.util.HashMap;
import java.util.Map;

public final class ElectromagnetDef {

    public final String name;
    public final int power;
    public final int heat;
    public final double magneticField;
    public final int maxTemp;
    public final int efficiency;

    public ElectromagnetDef(String name, int power, int heat, double magneticField, int maxTemp, int efficiency) {
        this.name = name;
        this.power = power;
        this.heat = heat;
        this.magneticField = magneticField;
        this.maxTemp = maxTemp;
        this.efficiency = efficiency;
    }

    private static final Map<String, ElectromagnetDef> BY_NAME = new HashMap<>();

    static {
        register("basic_electromagnet",             1000, 300, 0.2, 350000, 50);
        register("magnesium_diboride_electromagnet",2000, 580, 0.5,  39000, 80);
        register("niobium_tin_electromagnet",        4000,1140, 1.0,  18000, 90);
        register("niobium_titanium_electromagnet",   8000,2260, 2.0,  10000, 95);
        register("bscco_electromagnet",             16000,4500, 4.0, 104000, 99);
    }

    private static void register(String name, int power, int heat, double field, int maxTemp, int eff) {
        BY_NAME.put(name, new ElectromagnetDef(name, power, heat, field, maxTemp, eff));
    }

    public static ElectromagnetDef get(String blockName) {
        if (blockName.endsWith("_slope")) {
            return BY_NAME.get(blockName.substring(0, blockName.length() - "_slope".length()));
        }
        return BY_NAME.get(blockName);
    }

    public static boolean isElectromagnet(String blockName) {
        return get(blockName) != null;
    }
}
