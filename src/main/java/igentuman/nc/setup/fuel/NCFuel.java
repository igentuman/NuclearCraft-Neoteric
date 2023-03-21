package igentuman.nc.setup.fuel;

public class NCFuel {
    public String name;
    private FuelDef def;

    public void setDef(FuelDef def) {
        this.def = def;
    }

    public FuelDef getDefault()
    {
        return def;
    }

    public FuelDef getZirconiumAlloy()
    {
        return def;
    }

    public FuelDef getOxide()
    {
        return def;
    }

    public FuelDef getNitride()
    {
        return def;
    }

    private NCFuel()
    {

    }

    public static NCFuel of(FuelDef fuelDef) {
        NCFuel f = new NCFuel();
        f.setDef(fuelDef);
        return f;
    }
}
