package igentuman.nc.content.fuel;

import igentuman.nc.NuclearCraft;
import org.apache.logging.log4j.Level;

import static igentuman.nc.handler.config.FissionConfig.FUEL_CONFIG;

public class FuelDef {

    public final String name;
    public final String group;
    public double heat;
    public int criticality;
    public int depletion;
    public int efficiency;
    public int forge_energy;
    public int[] isotopes;
    
    // Recipe modifiers for automatic recipe generation
    public double timeModifier = 1.0;
    public double powerModifier = 1.0;
    public double radiationModifier = 1.0;

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
    
    public FuelDef recipeModifiers(double timeModifier, double powerModifier, double radiationModifier) {
        this.timeModifier = timeModifier;
        this.powerModifier = powerModifier;
        this.radiationModifier = radiationModifier;
        return this;
    }

    public FuelDef(String group, String name, int forge_energy, double heat, double criticality, double depletion, double efficiency) {
        this(group, name, forge_energy, heat, (int)criticality, (int)depletion, (int)efficiency);
    }

    private double boilingHeatMult()
    {
        return FUEL_CONFIG.HEAT_MULTIPLIER.get();
    }

    public double depletionMult()
    {
        return FUEL_CONFIG.DEPLETION_MULTIPLIER.get();
    }

    public double getHeatFEMode()
    {
        return heat*FUEL_CONFIG.FUEL_HEAT_MULTIPLIER.get();
    }

    public double getHeatBoilingMode() {
        double mult = boilingHeatMult();
        try {
            if(name.substring(0,1).equalsIgnoreCase("l")) {
                mult *=2;
            }
        } catch (NullPointerException ignore) {}

        return Math.ceil(heat*mult);
    }
}
