package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

public class FuelReprocessorRecipes {

    public static void fuelReprocessor(RecipeOutput out) {
        reproc(out, "americium/hea-242",
                iso("americium/243", 3), iso("americium/242",   1), iso("curium/246",    2),
                iso("berkelium/247", 1), dst("molybdenum",       1), dst("promethium_147", 1));

        reproc(out, "americium/lea-242",
                iso("americium/243", 3), iso("curium/243",       1), iso("curium/246",    3),
                iso("berkelium/248", 1), dst("molybdenum",       1), dst("promethium_147", 1));

        reproc(out, "thorium/tbu",
                iso("uranium/233",   1), iso("uranium/238",      5), iso("neptunium/236", 1),
                iso("neptunium/237", 1), dst("strontium_90",     1), dst("caesium_137",   1));

        reproc(out, "uranium/leu-233",
                iso("uranium/238",   5), iso("plutonium/241",    1), iso("plutonium/242", 1),
                iso("americium/243", 1), dst("strontium_90",     1), dst("caesium_137",   1));

        reproc(out, "uranium/heu-233",
                iso("uranium/235",   1), iso("uranium/238",      2), iso("plutonium/242", 3),
                iso("americium/243", 1), dst("strontium_90",     1), dst("caesium_137",   1));

        reproc(out, "uranium/leu-235",
                iso("uranium/238",   4), iso("plutonium/239",    1), iso("plutonium/242", 1),
                iso("americium/243", 1), dst("strontium_90",     1), dst("caesium_137",   1));

        reproc(out, "uranium/heu-235",
                iso("uranium/238",   2), iso("plutonium/239",    1), iso("plutonium/242", 3),
                iso("americium/243", 1), dst("strontium_90",     1), dst("caesium_137",   1));

        reproc(out, "neptunium/hen-236",
                iso("uranium/238",   4), iso("neptunium/237",    1), iso("plutonium/241", 1),
                iso("plutonium/242", 1), dst("molybdenum",       1), dst("caesium_137",   1));

        reproc(out, "neptunium/len-236",
                iso("plutonium/242", 5), iso("neptunium/237",    1), iso("plutonium/241", 1),
                iso("plutonium/242", 1), dst("molybdenum",       1), dst("caesium_137",   1));

        reproc(out, "plutonium/lep-239",
                iso("uranium/238",   4), iso("americium/243",    1), iso("curium/246",    1),
                iso("plutonium/242", 1), dst("promethium_147",   1), dst("strontium_90",  1));

        reproc(out, "plutonium/hep-239",
                iso("americium/243", 4), iso("curium/243",       1), iso("plutonium/241", 1),
                iso("plutonium/242", 1), dst("promethium_147",   1), dst("strontium_90",  1));

        reproc(out, "plutonium/lep-241",
                iso("plutonium/242", 5), iso("americium/243",    1), iso("curium/246",    1),
                iso("berkelium/247", 1), dst("promethium_147",   1), dst("strontium_90",  1));

        reproc(out, "plutonium/hep-241",
                iso("americium/243", 3), iso("americium/241",    1), iso("curium/246",    2),
                iso("plutonium/242", 1), dst("promethium_147",   1), dst("strontium_90",  1));

        reproc(out, "mixed/mix-239",
                iso("uranium/238",   4), iso("plutonium/239",    1), iso("plutonium/242", 1),
                iso("americium/243", 1), dst("strontium_90",     1), dst("caesium_137",   1));

        reproc(out, "mixed/mix-241",
                iso("uranium/238",   4), iso("neptunium/237",    1), iso("plutonium/241", 1),
                iso("plutonium/242", 1), dst("molybdenum",       1), dst("caesium_137",   1));

        reproc(out, "curium/lecm-243",
                iso("curium/246",    4), iso("curium/247",       1), iso("berkelium/247", 2),
                iso("berkelium/248", 1), dst("molybdenum",       1), dst("promethium_147", 1));

        reproc(out, "curium/hecm-243",
                iso("curium/245",    4), iso("berkelium/247",    2), iso("berkelium/248", 1),
                dst("molybdenum",    1), dst("promethium_147",   1));

        reproc(out, "curium/lecm-245",
                iso("curium/246",    4), iso("curium/247",       1), iso("berkelium/247", 2),
                iso("californium/249", 1), dst("molybdenum",     1), dst("europium_155",  1));

        reproc(out, "curium/hecm-245",
                iso("curium/246",    3), iso("curium/247",       1), iso("berkelium/247", 2),
                iso("californium/249", 1), dst("molybdenum",     1), dst("europium_155",  1));

        reproc(out, "curium/lecm-247",
                iso("curium/246",    5), iso("berkelium/247",    1), iso("californium/249", 1),
                iso("berkelium/248", 1), dst("molybdenum",       1), dst("europium_155",  1));

        reproc(out, "curium/hecm-247",
                iso("californium/251", 1), iso("californium/249", 1), iso("berkelium/247", 4),
                iso("berkelium/248", 1),   dst("molybdenum",      1), dst("europium_155", 1));

        reproc(out, "berkelium/leb-248",
                iso("berkelium/247", 5), iso("berkelium/248",    1), iso("californium/249", 1),
                iso("californium/251", 1), dst("ruthenium_106",  1), dst("promethium_147", 1));

        reproc(out, "berkelium/heb-248",
                iso("berkelium/248", 1), iso("californium/249",  1), iso("californium/251", 2),
                iso("californium/252", 3), dst("ruthenium_106",  1), dst("promethium_147", 1));

        reproc(out, "californium/lecf-249",
                iso("californium/252", 8), dst("ruthenium_106",  1), dst("promethium_147", 1));

        reproc(out, "californium/hecf-249",
                iso("californium/252", 6), iso("californium/250", 2),
                dst("ruthenium_106",   1), dst("promethium_147", 1));

        reproc(out, "californium/lecf-251",
                iso("californium/252", 8), dst("ruthenium_106",  1), dst("promethium_147", 1));

        reproc(out, "californium/hecf-251",
                iso("californium/252", 7), dst("ruthenium_106",  1), dst("promethium_147", 1));

        reproc(out, "xenorium/xen-298",
                iso("quantite",      1), iso("berkelium/247",    1), dst("promethium_147", 1));
    }
}
