package igentuman.nc.fluid;

public class AcidDefinition {
    public String name;
    public int density = 200;

    public int color = 0xCCFFFFFF;

    public int damage = 3;
    public int temperature = 400;

    public AcidDefinition(String name, int density, int color, int damage) {
        this.name = name;
        this.density = density;
        this.color = color;
        this.damage = damage;
    }

    public AcidDefinition(String name, int color) {
        this.name = name;
        this.color = color;
    }
}
