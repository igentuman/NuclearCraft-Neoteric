package igentuman.nc.content.fuel;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.FusionConfig;
import org.apache.logging.log4j.Level;

import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.handler.config.FissionConfig.FUEL_CONFIG;

public class FuelDef {

    public final String name;
    public final String group;
    public double heat;
    public int criticality;
    public int depletion;
    public int efficiency;
    public int forge_energy;

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

    public FuelDef isotopes(int... isotopes) {
        this.isotopes = isotopes;
        return this;
    }

    public int[] isotopes;

    private boolean initialized = false;

    public FuelDef(String group, String name, int forge_energy, double heat, double criticality, double depletion, double efficiency) {
        this(group, name, forge_energy, heat, (int)criticality, (int)depletion, (int)efficiency);
    }

    private Double heatMult()
    {
        if(!FusionConfig.isLoaded()) {
            return 3.24444444;
        }
        return FUEL_CONFIG.HEAT_MULTIPLIER.get();
    }

    public FuelDef config()
    {
        if(!initialized) {
            initialized = true;
            NuclearCraft.LOGGER.log(Level.INFO,"FuelDef: "+group+" "+name);
            int id = FuelManager.all().get(group).keySet().stream().toList().indexOf(name);
            efficiency = FUEL_CONFIG.EFFICIENCY.get().get(id);
            criticality = FUEL_CONFIG.CRITICALITY.get().get(id);
            heat = FUEL_CONFIG.HEAT.get().get(id)*FUEL_CONFIG.FUEL_HEAT_MULTIPLIER.get();
            depletion = (int) (FUEL_CONFIG.DEPLETION.get().get(id)*FUEL_CONFIG.DEPLETION_MULTIPLIER.get());
        }
        return this;
    }


    public double getHeatFEMode()
    {
        return config().heat*FUEL_CONFIG.FUEL_HEAT_MULTIPLIER.get();
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
