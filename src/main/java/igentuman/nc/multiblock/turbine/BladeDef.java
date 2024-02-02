package igentuman.nc.multiblock.turbine;

public class BladeDef {
    public double efficiency = 0;
    public double expansion = 0;
    public String name = "";

    public BladeDef() {
    }

    public BladeDef(String name, double efficiency, double expansion) {
        this.efficiency = efficiency;
        this.expansion = expansion;
        this.name = name;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getExpansion() {
        return expansion;
    }
}
