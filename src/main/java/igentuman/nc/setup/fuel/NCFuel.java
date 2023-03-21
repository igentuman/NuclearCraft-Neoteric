package igentuman.nc.setup.fuel;

public class NCFuel {
    public String name;
    private FuelDef def;
    private FuelDef oxide;
    private FuelDef nitride;
    private FuelDef zirconium;
    private FuelDef triso;

    public void setDef(FuelDef def) {
        this.oxide = def;
    }

    public FuelDef getDefault()
    {
        if(def == null) {
            def = new FuelDef(name,
                    (float)oxide.heat/1.4, (float)oxide.criticality*1.1,
                    (float)oxide.depletion/1.1, (float)oxide.efficiency/1.05);
        }
        return def;
    }

    public FuelDef getZirconiumAlloy()
    {
        if(zirconium == null) {
            zirconium = new FuelDef(name,
                    (float)oxide.heat*1.25, (float)oxide.criticality/1.25,
                    (float)oxide.depletion/1.25, (float)oxide.efficiency/1.01);
        }
        return zirconium;
    }

    public FuelDef getOxide()
    {
        return oxide;
    }

    public FuelDef getNitride()
    {
        if(nitride == null) {
            nitride = new FuelDef(name,
                    (float)oxide.heat/1.25, (float)oxide.criticality*1.25,
                    (float)oxide.depletion*1.25, (float)oxide.efficiency/1.01);
        }
        return nitride;
    }

    public FuelDef getTriso() {
        if(triso == null) {
            triso = new FuelDef(name,
                    (float)oxide.heat*1.5, (float)oxide.criticality/1.5,
                    (float)oxide.depletion/1.25, (float)oxide.efficiency*1.5);
        }
        return triso;
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
