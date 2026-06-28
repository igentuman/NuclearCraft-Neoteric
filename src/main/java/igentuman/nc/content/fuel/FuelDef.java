package igentuman.nc.content.fuel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameter set for a single fission fuel form. A base fuel (e.g. {@code uranium/heu-233})
 * carries the authored values; its oxide / nitride / zirconium-alloy / triso variants are
 * derived from the base via {@link #variants()} using the same formulas as the original
 * NuclearCraft Neoteric {@code NCFuel}.
 *
 * <p>This is the single shared definition type: built-in fuels and KubeJS-registered fuels
 * both produce {@code FuelDef} instances that flow through the same registration path.</p>
 */
public class FuelDef {

    /** Item-form suffixes that get a fuel/depleted item. Triso ({@code _tr}) is item-only. */
    public static final String[] ITEM_VARIANTS = {"", "_ox", "_ni", "_za", "_tr"};
    /** Variants that additionally get molten + depleted-molten fluids (no triso fluid). */
    public static final String[] FLUID_VARIANTS = {"", "_za", "_ox", "_ni"};

    public final String group;
    public final String name;

    public int forgeEnergy;
    public double heat;
    public int criticality;
    public int depletion;
    public int efficiency;
    public int[] isotopes = {0, 0};

    // Recipe modifiers carried as fuel metadata (no recipes are generated yet).
    public double timeModifier = 1.0;
    public double powerModifier = 1.0;
    public double radiationModifier = 1.0;

    public FuelDef(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public FuelDef(String group, String name, int forgeEnergy, double heat, int criticality, int depletion, int efficiency) {
        this(group, name);
        this.forgeEnergy = forgeEnergy;
        this.heat = heat;
        this.criticality = criticality;
        this.depletion = depletion;
        this.efficiency = efficiency;
    }

    public FuelDef forgeEnergy(int forgeEnergy) { this.forgeEnergy = forgeEnergy; return this; }
    public FuelDef heat(double heat) { this.heat = heat; return this; }
    public FuelDef criticality(int criticality) { this.criticality = criticality; return this; }
    public FuelDef depletion(int depletion) { this.depletion = depletion; return this; }
    public FuelDef efficiency(int efficiency) { this.efficiency = efficiency; return this; }

    public FuelDef isotopes(int a, int b) { this.isotopes = new int[]{a, b}; return this; }

    public FuelDef recipeModifiers(double time, double power, double radiation) {
        this.timeModifier = time;
        this.powerModifier = power;
        this.radiationModifier = radiation;
        return this;
    }

    /** {@code true} for families that only ever get the base form (no cladding variants). */
    public boolean isSpecial() {
        return group.matches("xenorium.*|quantite.*");
    }

    /**
     * Resolves the parameter set for each item variant. Special fuels yield only the base form.
     * Derivation mirrors the original mod: oxide is derived from the base; nitride, zirconium-alloy
     * and triso are derived from the oxide.
     */
    public Map<String, FuelDef> variants() {
        Map<String, FuelDef> map = new LinkedHashMap<>();
        map.put("", this);
        if (isSpecial()) {
            return map;
        }
        FuelDef oxide = oxide();
        map.put("_ox", oxide);
        map.put("_ni", nitride(oxide));
        map.put("_za", zirconium(oxide));
        map.put("_tr", triso(oxide));
        return map;
    }

    private FuelDef copyMeta(FuelDef def) {
        def.isotopes = this.isotopes;
        def.timeModifier = this.timeModifier;
        def.powerModifier = this.powerModifier;
        def.radiationModifier = this.radiationModifier;
        return def;
    }

    private FuelDef oxide() {
        return copyMeta(new FuelDef(group, name,
                (int) (forgeEnergy * 1.4),
                heat * 1.25,
                (int) (criticality * 1.1),
                (int) (depletion / 1.1),
                (int) (efficiency / 1.05)));
    }

    private FuelDef nitride(FuelDef oxide) {
        return copyMeta(new FuelDef(group, name,
                (int) (forgeEnergy * 1.6),
                Math.ceil(oxide.heat * 1.5),
                (int) (oxide.criticality * 1.25),
                (int) (oxide.depletion / 1.25),
                (int) (oxide.efficiency / 1.01)));
    }

    private FuelDef zirconium(FuelDef oxide) {
        return copyMeta(new FuelDef(group, name,
                (int) (forgeEnergy * 1.25),
                Math.ceil(oxide.heat * 1.1),
                (int) (oxide.criticality / 1.25),
                (int) (oxide.depletion * 1.05),
                (int) (oxide.efficiency / 1.01)));
    }

    private FuelDef triso(FuelDef oxide) {
        return copyMeta(new FuelDef(group, name,
                0,
                Math.ceil(oxide.heat * 1.5),
                (int) (oxide.criticality / 1.5),
                (int) (oxide.depletion * 12.5),
                (int) (oxide.efficiency * 1.5)));
    }
}
