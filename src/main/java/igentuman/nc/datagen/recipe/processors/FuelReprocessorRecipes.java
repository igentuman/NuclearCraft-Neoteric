package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import java.util.LinkedHashMap;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates fuel reprocessor recipes and holds the depleted-fuel output table shared with the centrifuge. */
public class FuelReprocessorRecipes {

    /** One reprocessing output: an isotope or a material dust, with a per-fuel-unit count. */
    public record RpOut(String name, int count, boolean isotope) {}

    /** A depleted-fuel reprocessing recipe: process time plus its outputs. */
    public record Recipe(int time, RpOut... outs) {}

    private static RpOut ri(String name, int count) { return new RpOut(name, count, true); }
    private static RpOut rd(String name, int count) { return new RpOut(name, count, false); }

    /** Depleted-fuel key -> reprocessing recipe. Shared by the item path (fuel_reprocessor) and the
     *  molten path (centrifuge), so the two never diverge. */
    public static final LinkedHashMap<String, Recipe> TABLE = new LinkedHashMap<>();
    static {
        TABLE.put("americium/hea-242", new Recipe(300, ri("americium/243",3), ri("americium/242",1), ri("curium/246",2), ri("berkelium/247",1), rd("molybdenum",1), rd("promethium_147",1)));
        TABLE.put("americium/lea-242", new Recipe(100, ri("americium/243",3), ri("curium/243",1), ri("curium/246",3), ri("berkelium/248",1), rd("molybdenum",1), rd("promethium_147",1)));
        TABLE.put("thorium/tbu", new Recipe(100, ri("uranium/233",1), ri("uranium/238",5), ri("neptunium/236",1), ri("neptunium/237",1), rd("strontium_90",1), rd("caesium_137",1)));
        TABLE.put("uranium/leu-233", new Recipe(100, ri("uranium/238",5), ri("plutonium/241",1), ri("plutonium/242",1), ri("americium/243",1), rd("strontium_90",1), rd("caesium_137",1)));
        TABLE.put("uranium/heu-233", new Recipe(300, ri("uranium/235",1), ri("uranium/238",2), ri("plutonium/242",3), ri("americium/243",1), rd("strontium_90",1), rd("caesium_137",1)));
        TABLE.put("uranium/leu-235", new Recipe(100, ri("uranium/238",4), ri("plutonium/239",1), ri("plutonium/242",1), ri("americium/243",1), rd("strontium_90",1), rd("caesium_137",1)));
        TABLE.put("uranium/heu-235", new Recipe(300, ri("uranium/238",2), ri("plutonium/239",1), ri("plutonium/242",3), ri("americium/243",1), rd("strontium_90",1), rd("caesium_137",1)));
        TABLE.put("neptunium/hen-236", new Recipe(100, ri("uranium/238",4), ri("neptunium/237",1), ri("plutonium/241",1), ri("plutonium/242",1), rd("molybdenum",1), rd("caesium_137",1)));
        TABLE.put("neptunium/len-236", new Recipe(300, ri("plutonium/242",5), ri("neptunium/237",1), ri("plutonium/241",1), ri("plutonium/242",1), rd("molybdenum",1), rd("caesium_137",1)));
        TABLE.put("plutonium/lep-239", new Recipe(100, ri("uranium/238",4), ri("americium/243",1), ri("curium/246",1), ri("plutonium/242",1), rd("promethium_147",1), rd("strontium_90",1)));
        TABLE.put("plutonium/hep-239", new Recipe(300, ri("americium/243",4), ri("curium/243",1), ri("plutonium/241",1), ri("plutonium/242",1), rd("promethium_147",1), rd("strontium_90",1)));
        TABLE.put("plutonium/lep-241", new Recipe(100, ri("plutonium/242",5), ri("americium/243",1), ri("curium/246",1), ri("berkelium/247",1), rd("promethium_147",1), rd("strontium_90",1)));
        TABLE.put("plutonium/hep-241", new Recipe(300, ri("americium/243",3), ri("americium/241",1), ri("curium/246",2), ri("plutonium/242",1), rd("promethium_147",1), rd("strontium_90",1)));
        TABLE.put("mixed/mix-239", new Recipe(100, ri("uranium/238",4), ri("plutonium/239",1), ri("plutonium/242",1), ri("americium/243",1), rd("strontium_90",1), rd("caesium_137",1)));
        TABLE.put("mixed/mix-241", new Recipe(100, ri("uranium/238",4), ri("neptunium/237",1), ri("plutonium/241",1), ri("plutonium/242",1), rd("molybdenum",1), rd("caesium_137",1)));
        TABLE.put("curium/lecm-243", new Recipe(100, ri("curium/246",4), ri("curium/247",1), ri("berkelium/247",2), ri("berkelium/248",1), rd("molybdenum",1), rd("promethium_147",1)));
        TABLE.put("curium/hecm-243", new Recipe(300, ri("curium/245",4), ri("berkelium/247",2), ri("berkelium/248",1), rd("molybdenum",1), rd("promethium_147",1)));
        TABLE.put("curium/lecm-245", new Recipe(100, ri("curium/246",4), ri("curium/247",1), ri("berkelium/247",2), ri("californium/249",1), rd("molybdenum",1), rd("europium_155",1)));
        TABLE.put("curium/hecm-245", new Recipe(300, ri("curium/246",3), ri("curium/247",1), ri("berkelium/247",2), ri("californium/249",1), rd("molybdenum",1), rd("europium_155",1)));
        TABLE.put("curium/lecm-247", new Recipe(100, ri("curium/246",5), ri("berkelium/247",1), ri("californium/249",1), ri("berkelium/248",1), rd("molybdenum",1), rd("europium_155",1)));
        TABLE.put("curium/hecm-247", new Recipe(300, ri("californium/251",1), ri("californium/249",1), ri("berkelium/247",4), ri("berkelium/248",1), rd("molybdenum",1), rd("europium_155",1)));
        TABLE.put("berkelium/leb-248", new Recipe(100, ri("berkelium/247",5), ri("berkelium/248",1), ri("californium/249",1), ri("californium/251",1), rd("ruthenium_106",1), rd("promethium_147",1)));
        TABLE.put("berkelium/heb-248", new Recipe(300, ri("berkelium/248",1), ri("californium/249",1), ri("californium/251",2), ri("californium/252",3), rd("ruthenium_106",1), rd("promethium_147",1)));
        TABLE.put("californium/lecf-249", new Recipe(100, ri("californium/252",8), rd("ruthenium_106",1), rd("promethium_147",1)));
        TABLE.put("californium/hecf-249", new Recipe(300, ri("californium/252",6), ri("californium/250",2), rd("ruthenium_106",1), rd("promethium_147",1)));
        TABLE.put("californium/lecf-251", new Recipe(100, ri("californium/252",8), rd("ruthenium_106",1), rd("promethium_147",1)));
        TABLE.put("californium/hecf-251", new Recipe(300, ri("californium/252",7), rd("ruthenium_106",1), rd("promethium_147",1)));
        TABLE.put("xenorium/xen-298", new Recipe(1100, ri("quantite",1), ri("berkelium/247",1), rd("promethium_147",1)));
    }

    public static void fuelReprocessor(RecipeOutput out) {
        for (var e : TABLE.entrySet()) {
            RpOut[] o = e.getValue().outs();
            I[] items = new I[o.length];
            for (int i = 0; i < o.length; i++) {
                items[i] = o[i].isotope() ? iso(o[i].name(), o[i].count()) : dst(o[i].name(), o[i].count());
            }
            reproc(out, e.getKey(), items, e.getValue().time());
        }
    }
}
