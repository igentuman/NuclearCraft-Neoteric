package igentuman.nc.content.fuel;

public class NCFuel {
    public String group;

    public String name;
    private FuelDef def;
    private FuelDef oxide;
    private FuelDef nitride;
    private FuelDef zirconium;
    private FuelDef triso;

    public void setDef(FuelDef def) {
        this.def = def;
    }

    public FuelDef getDefault()
    {
        return def;
    }

    public FuelDef getZirconiumAlloy()
    {
        if(zirconium == null) {
            zirconium = new FuelDef(group, name,
                    (int) (def.forge_energy*1.25),
                    Math.ceil((float)oxide.heat*1.1), (float)oxide.criticality/1.25,
                    (float)oxide.depletion*1.05, (float)oxide.efficiency/1.01)
                    .isotopes(def.isotopes);
        }
        return zirconium;
    }

    public FuelDef getOxide()
    {
        if(oxide == null) {
            oxide = new FuelDef(group, name,
                    (int) (def.forge_energy*1.4),
                    (float)def.heat*1.25, (float)def.criticality*1.1,
                    (float)def.depletion/1.1, (float)def.efficiency/1.05)
                    .isotopes(def.isotopes);
        }
        return oxide;
    }

    public FuelDef getNitride()
    {
        if(nitride == null) {
            nitride = new FuelDef(group, name,
                    (int) (def.forge_energy*1.6),
                    Math.ceil((float)oxide.heat*1.5), (float)oxide.criticality*1.25,
                    (float)oxide.depletion/1.25, (float)oxide.efficiency/1.01)
                    .isotopes(def.isotopes);
        }
        return nitride;
    }

    public FuelDef getTriso() {
        if(triso == null) {
            triso = new FuelDef(group, name,
                    0,
                    Math.ceil((float)oxide.heat*1.5), (float)oxide.criticality/1.5,
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
