package igentuman.nc.multiblock.particle_chamber;

public class DetectorDef {

    public double efficiency = 0;
    public int power = 0;
    public int distance = 0;
    public String name = "";
    public String[] rules;

    public static DetectorDef make(String name, double efficiency, int power, int distance) {
        DetectorDef def = new DetectorDef();
        def.name = name;
        def.efficiency = efficiency;
        def.power = power;
        def.distance = distance;
        return def;
    }

    private DetectorDef() {
    }

}
