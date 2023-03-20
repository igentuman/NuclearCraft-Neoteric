package igentuman.nc.setup.materials;

public class NCMaterial {
    public String name;
    public boolean normal_ore = true;
    public boolean nether_ore = false;
    public boolean end_ore = false;
    public boolean deepslate_ore = true;

    public boolean chunk = true;
    public boolean ingot = true;
    public boolean nugget = true;
    public boolean block = true;
    public boolean fluid = true;
    public int temperature = 1000;
    public int density = 200;
    public boolean isGas = false;

    public boolean plate = true;
    public boolean dust = true;
    public int color = 0;

    private NCMaterial(String name)
    {
        this.name = name;
    }

    public static NCMaterial get(String name)
    {
        return new NCMaterial(name);
    }

    public static NCMaterial ore(String name)
    {
        return get(name);
    }

    public static NCMaterial metal(String name)
    {
        return get(name)
                .ores(false, false, false, false)
                .products(true, true, true, true, true, true)
                .fluid(true, 1000);
    }

    public NCMaterial ores(boolean normal_ore, boolean deepslate_ore, boolean nether_ore, boolean end_ore)
    {
        this.normal_ore = normal_ore;
        this.deepslate_ore = deepslate_ore;
        this.nether_ore = nether_ore;
        this.end_ore = end_ore;
        return this;
    }

    public NCMaterial products(boolean chunk, boolean dust, boolean ingot, boolean nugget, boolean block, boolean plate)
    {
        this.chunk = chunk;
        this.dust = dust;
        this.ingot = ingot;
        this.nugget = nugget;
        this.block = block;
        this.plate = plate;
        return this;
    }

    public NCMaterial color(int color)
    {
        this.color = color;
        return this;
    }

    public NCMaterial fluid(boolean fluid, int temperature)
    {
        this.fluid = fluid;
        this.temperature = temperature;
        return this;
    }
}
