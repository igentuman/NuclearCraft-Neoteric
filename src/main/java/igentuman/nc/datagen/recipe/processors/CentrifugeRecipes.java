package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.CENTRIFUGE;

/** Generates centrifuge recipes that separate fluids into components, including molten depleted-fuel reprocessing. */
public class CentrifugeRecipes {

    public static void centrifuge(RecipeOutput out) {
        String id = CENTRIFUGE;
        split(out, id, "carbon_dioxide", fluidOf("carbon_dioxide"), 1000,
                new F[]{fluid("carbon_dioxide", 125), fluid("carbon_monoxide", 125), fluid("carbon", 375), fluid("oxygen", 375)});
        split(out, id, "irradiated_boron", fluidOf("irradiated_boron"), 1000,
                new F[]{f(isotopeFluid("boron/10", ""), 500), f(isotopeFluid("boron/11", ""), 500)}, 300);
        split(out, id, "irradiated_lithium", fluidOf("irradiated_lithium"), 1000,
                new F[]{f(isotopeFluid("lithium/6", ""), 250), f(isotopeFluid("lithium/7", ""), 250), fluid("tritium", 500)});
        split(out, id, "technical_water", fluidOf("technical_water"), 1000,
                new F[]{fluid("deuterium", 750), fluid("oxygen", 250)}, 380);
        split(out, id, "uranium", fluidOf("uranium"), 160,
                new F[]{f(isotopeFluid("uranium/238", ""), MOLTEN_INGOT), f(isotopeFluid("uranium/235", ""), 10)}, 180);
        split(out, id, "fissile_fuel", fluidOf("fissile_fuel"), 500,
                new F[]{f(isotopeFluid("uranium/238", ""), 300), f(isotopeFluid("uranium/235", ""), 80), f(isotopeFluid("uranium/233", ""), 80),
                fluid("hydrofluoric_acid", 10), fluid("sulfuric_acid", 10)}, 380);
        split(out, id, "nuclear_waste", fluidOf("nuclear_waste"), 50,
                new F[]{fluid("polonium", 5), f(isotopeFluid("plutonium/238", ""), 5), f(isotopeFluid("plutonium/242", ""), 5),
                f(isotopeFluid("plutonium/241", ""), 5), fluid("spent_nuclear_waste", 1)}, 3000);

        for (var e : FuelReprocessorRecipes.TABLE.entrySet()) {
            LinkedHashMap<Fluid, Integer> merged = new LinkedHashMap<>();
            boolean ok = true;
            for (FuelReprocessorRecipes.RpOut o : e.getValue().outs()) {
                Fluid fOut = o.isotope() ? isotopeFluid(o.name(), "") : fluidOf(o.name());
                if (fOut == null) { ok = false; break; }
                merged.merge(fOut, MOLTEN_INGOT * o.count(), Integer::sum);
            }
            if (!ok) continue;
            F[] outs = new F[merged.size()];
            int i = 0;
            for (var en : merged.entrySet()) outs[i++] = f(en.getKey(), en.getValue());
            String name = "depleted_" + e.getKey().replace('/', '_').replace('-', '_');
            split(out, id, name, depletedFuelFluid(e.getKey(), ""), MOLTEN_INGOT * 9, outs, e.getValue().time());
        }

        for (String material : LeacherRecipes.SLURRY_MATERIALS) {
            split(out, id, material + "_slurry", fluidOf(material + "_slurry"), 1000,
                    new F[]{fluid(material + "_clean_slurry", 800), fluid("hydrochloric_acid", 50),
                    fluid("nitric_acid", 50), fluid("calcium_sulfate_solution", 10)});
        }
    }
}
