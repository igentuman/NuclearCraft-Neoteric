package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.DECAY_HASTENER;

public class DecayHastenerRecipes {

    public static void decayHastener(RecipeOutput out) {
        i2i(out, DECAY_HASTENER, "irradiated_borax",   dust("irradiated_borax"),       dust("borax"),            2, 400);
        i2i(out, DECAY_HASTENER, "strontium_90",       dust("strontium_90"),            dust("strontium"),        2, 400);
        i2i(out, DECAY_HASTENER, "radium",             dust("radium"),                  dust("lead"),             1);
        i2i(out, DECAY_HASTENER, "polonium",           dust("polonium"),                dust("lead"),             1);
        i2i(out, DECAY_HASTENER, "protactinium_233",   dust("protactinium_233"),        isotope("uranium/233"),   1);
        i2i(out, DECAY_HASTENER, "bismuth",            dust("bismuth"),                 dust("thallium"),         1);
        i2i(out, DECAY_HASTENER, "tbp",                dust("tbp"),                     isotope("thorium/230"),   1);
        i2i(out, DECAY_HASTENER, "ruthenium_106",      dust("ruthenium_106"),           dust("palladium"),        1);
        i2i(out, DECAY_HASTENER, "caesium_137",        dust("caesium_137"),             dust("barium"),           1);
        i2i(out, DECAY_HASTENER, "promethium_147",     dust("promethium_147"),          dust("neodymium"),        1);
        i2i(out, DECAY_HASTENER, "europium_155",       dust("europium_155"),            dust("gadolinium"),       1);
        i2i(out, DECAY_HASTENER, "uranium_233",        isotope("uranium/233"),          dust("bismuth"),          1);
        i2i(out, DECAY_HASTENER, "uranium_235",        isotope("uranium/235"),          dust("lead"),             1);
        i2i(out, DECAY_HASTENER, "uranium_238",        isotope("uranium/238"),          dust("radium"),           1);
        i2i(out, DECAY_HASTENER, "neptunium_236",      isotope("neptunium/236"),        dust("thorium"),          1);
        i2i(out, DECAY_HASTENER, "neptunium_237",      isotope("neptunium/237"),        isotope("uranium/233"),   1);
        i2i(out, DECAY_HASTENER, "plutonium_238",      isotope("plutonium/238"),        dust("lead"),             1);
        i2i(out, DECAY_HASTENER, "plutonium_239",      isotope("plutonium/239"),        isotope("uranium/233"),   1);
        i2i(out, DECAY_HASTENER, "plutonium_241",      isotope("plutonium/241"),        isotope("neptunium/237"), 1);
        i2i(out, DECAY_HASTENER, "plutonium_242",      isotope("plutonium/242"),        isotope("uranium/238"),   1);
        i2i(out, DECAY_HASTENER, "americium_241",      isotope("americium/241"),        isotope("neptunium/237"), 1);
        i2i(out, DECAY_HASTENER, "americium_242",      isotope("americium/242"),        dust("lead"),             1);
        i2i(out, DECAY_HASTENER, "americium_243",      isotope("americium/243"),        isotope("plutonium/238"), 1);
        i2i(out, DECAY_HASTENER, "curium_243",         isotope("curium/243"),           isotope("plutonium/239"), 1);
        i2i(out, DECAY_HASTENER, "curium_245",         isotope("curium/245"),           isotope("plutonium/241"), 1);
        i2i(out, DECAY_HASTENER, "curium_246",         isotope("curium/246"),           isotope("americium/242"), 1);
        i2i(out, DECAY_HASTENER, "curium_247",         isotope("curium/247"),           isotope("americium/243"), 1);
        i2i(out, DECAY_HASTENER, "berkelium_247",      isotope("berkelium/247"),        isotope("americium/243"), 1);
        i2i(out, DECAY_HASTENER, "berkelium_248",      isotope("berkelium/248"),        dust("thorium"),          1);
        i2i(out, DECAY_HASTENER, "californium_249",    isotope("californium/249"),      isotope("curium/245"),    1);
        i2i(out, DECAY_HASTENER, "californium_250",    isotope("californium/250"),      isotope("curium/246"),    1);
        i2i(out, DECAY_HASTENER, "californium_251",    isotope("californium/251"),      isotope("curium/247"),    1);
        i2i(out, DECAY_HASTENER, "californium_252",    isotope("californium/252"),      dust("thorium"),          1);
        i2i(out, DECAY_HASTENER, "xenorium_298",       isotope("xenorium/298"),         isotope("quantite"),      1);
    }
}
