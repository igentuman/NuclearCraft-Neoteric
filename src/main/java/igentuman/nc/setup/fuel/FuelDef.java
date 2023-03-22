package igentuman.nc.setup.fuel;

import igentuman.nc.handler.config.CommonConfig;

import static igentuman.nc.handler.config.CommonConfig.FUEL_CONFIG;
import static igentuman.nc.handler.config.CommonConfig.FuelConfig.HEAT_MULTIPLIER;

public class FuelDef {

    public FuelDef(String group, String name, int forge_energy, double heat, int criticality, int depletion, int efficiency)
    {
        this.group = group;
        this.name = name;
        this.heat = heat;
        this.forge_energy = forge_energy;
        this.criticality = criticality;
        this.depletion = depletion;
        this.efficiency = efficiency;
    }

    private boolean initialized = false;

    public String name = "";

    public String group = "";
    public double heat = 0;

    public int criticality = 0;

    public int depletion = 0;

    public int efficiency = 0;
    public int forge_energy = 0;

    public FuelDef(String group, String name, int forge_energy, double heat, double criticality, double depletion, double efficiency) {
        this(group, name, forge_energy, heat, (int)criticality, (int)depletion, (int)efficiency);
    }

    private Double heatMult()
    {
        if(!CommonConfig.isLoaded()) {
            return 3.24444444;
        }
        return HEAT_MULTIPLIER.get();
    }

    public FuelDef config()
    {
        if(!CommonConfig.isLoaded()) {
            return this;
        }
        if(!initialized) {
            initialized = true;
            int id = FuelManager.all().get(group).keySet().stream().toList().indexOf(name);
            efficiency = FUEL_CONFIG.EFFICIENCY.get().get(id);
            criticality = FUEL_CONFIG.CRITICALITY.get().get(id);
            heat = FUEL_CONFIG.HEAT.get().get(id);
            depletion = (int) (FUEL_CONFIG.DEPLETION.get().get(id)*FUEL_CONFIG.DEPLETION_MULTIPLIER.get());
        }
        return this;
    }


    public double getHeatFEMode()
    {
        return config().heat;
    }

    public double getHeatBoilingMode() {
        double mult = heatMult();
        try {
            if(name.substring(0,1).equalsIgnoreCase("l")) {
                mult *=2;
            }
        } catch (NullPointerException ignore) {}

        return Math.ceil(config().heat*mult);
    }
}
