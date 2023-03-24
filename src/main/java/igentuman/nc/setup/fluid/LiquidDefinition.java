package igentuman.nc.setup.fluid;

public class LiquidDefinition {
    public String name;
    public int density = 400;

    public int color = 0xCCFFFFFF;

    public int damage = 0;
    public int temperature = 440;

    public LiquidDefinition(String name, int density, int color, int damage) {
        this.name = name;
        this.density = density;
        this.color = color;
        this.damage = damage;
    }

    public LiquidDefinition(String name, int color) {
        this.name = name;
        this.color = color;
    }
}
