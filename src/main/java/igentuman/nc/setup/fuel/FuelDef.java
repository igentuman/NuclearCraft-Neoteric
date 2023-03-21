package igentuman.nc.setup.fuel;

import igentuman.nc.handler.config.CommonConfig;

public class FuelDef {

    public FuelDef(String name, int heat, int criticality, int depletion, int efficiency)
    {
        this.name = name;
        this.heat = heat;
        this.criticality = criticality;
        this.depletion = depletion;
        this.efficiency = efficiency;
    }

    private boolean initialized = false;

    public String name;
    public Integer heat;

    public Integer criticality;

    public Integer depletion;

    public Integer efficiency;

    public FuelDef config()
    {
        if(!initialized) {
            initialized = true;
           // efficiency = CommonConfig.FUEL_CONFIG.EFFICIENCY.get(FuelManager.all().get(name).).get("efficiency").get();
        }
        return this;
    }
}
