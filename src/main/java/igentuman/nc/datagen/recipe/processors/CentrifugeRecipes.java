package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.CENTRIFUGE;

public class CentrifugeRecipes {

    public static void centrifuge(RecipeOutput out) {
        String id = CENTRIFUGE;
        split(out, id, "carbon_dioxide", fluidOf("carbon_dioxide"), 1000,
                new F[]{fl("carbon_dioxide", 125), fl("carbon_monoxide", 125), fl("carbon", 375), fl("oxygen", 375)});
        split(out, id, "irradiated_boron", fluidOf("irradiated_boron"), 1000,
                new F[]{fl("boron10", 500), fl("boron11", 500)}, 300);
        split(out, id, "irradiated_lithium", fluidOf("irradiated_lithium"), 1000,
                new F[]{fl("lithium6", 250), fl("lithium7", 250), fl("tritium", 500)});
        split(out, id, "technical_water", fluidOf("technical_water"), 1000,
                new F[]{fl("deuterium", 750), fl("oxygen", 250)}, 380);
        split(out, id, "uranium", fluidOf("uranium"), 160,
                new F[]{fl("uranium238", MOLTEN_INGOT), fl("uranium235", 10)}, 180);
        split(out, id, "fissile_fuel", fluidOf("fissile_fuel"), 500,
                new F[]{fl("uranium238", 300), fl("uranium235", 80), fl("uranium233", 80),
                fl("hydrofluoric_acid", 10), fl("sulfuric_acid", 10)}, 380);
        split(out, id, "nuclear_waste", fluidOf("nuclear_waste"), 50,
                new F[]{fl("polonium", 5), fl("plutonium238", 5), fl("plutonium242", 5),
                fl("plutonium241", 5), fl("spent_nuclear_waste", 1)}, 3000);

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
    }
}
