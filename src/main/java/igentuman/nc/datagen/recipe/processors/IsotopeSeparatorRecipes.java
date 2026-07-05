package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

public class IsotopeSeparatorRecipes {

    public static void isotopeSeparator(RecipeOutput out) {
        sep(out, "lithium",    dust("lithium"),    10, isotope("lithium/7"),    9, isotope("lithium/6"),    1, 1000);
        sep(out, "yellowcake", dust("yellowcake"), 10, isotope("uranium/238"),  7, isotope("uranium/235"),  2, 1000);
        sep(out, "uranium",    dust("uranium"),    10, isotope("uranium/238"),  9, isotope("uranium/235"),  1, 800);
        sep(out, "thorium",    dust("thorium"),    10, isotope("thorium/232"),  9, isotope("thorium/230"),  1, 800);
        for (var e : ModEntries.FISSION_FUEL.entrySet()) {
            FissionFuelEntry fe = e.getValue();
            if (fe.group.equals("mixed")) continue;
            int[] iso = fe.base().isotopes;
            boolean h = fe.name.startsWith("h");
            sep(out, fe.group + "_" + fe.name.replace("-", "_"),
                    fuel(e.getKey(), ""), 3,
                    isotope(fe.group + "/" + iso[0]), h ? 3 : 1,
                    isotope(fe.group + "/" + iso[1]), h ? 6 : 8, 400);
        }
    }
}
