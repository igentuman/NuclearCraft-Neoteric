package igentuman.nc.setup.fuel;

import static igentuman.nc.handler.config.CommonConfig.FuelConfig.H_MULTIPLIER;

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

    public FuelDef(String name, double heat, double criticality, double depletion, double efficiency) {
        this(name, (int)heat, (int)criticality, (int)depletion, (int)efficiency);
    }

    private Double heatMult()
    {
        try {
            return H_MULTIPLIER.get();
        } catch (IllegalStateException ignored)
        {}
        return 3.24444444;
    }

    public FuelDef config()
    {
        if(!initialized) {
            initialized = true;
           // efficiency = CommonConfig.FUEL_CONFIG.EFFICIENCY.get(FuelManager.all().get(name).).get("efficiency").get();
        }
        return this;
    }

    public int getHeatBoiling() {
        double mult = heatMult();
        try {
            if(name.substring(0,1).equalsIgnoreCase("l")) {
                mult *=2;
            }
        } catch (NullPointerException ignore) {}

        return (int) (heat*mult);
    }
}
