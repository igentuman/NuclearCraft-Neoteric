package igentuman.nc.setup.registration.fluid;

public class GasDefinition {
    public String name;
    public int density = -1000;

    public int color = 0xCCFFFFFF;

    public int damage = 0;
    public int temperature = 0;

    public GasDefinition(String name, int density, int color, int damage) {
        this.name = name;
        this.density = density;
        this.color = color;
        this.damage = damage;
    }

    public GasDefinition(String name, int color) {
        this.name = name;
        this.color = color;
    }
}
