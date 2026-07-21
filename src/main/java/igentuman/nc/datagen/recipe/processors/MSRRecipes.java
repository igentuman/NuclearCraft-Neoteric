package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.datagen.recipe.UniversalProcessorRecipeBuilder;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;

public class MSRRecipes {

    public static void msr(RecipeOutput out) {
        for (FissionFuelEntry fe : ModEntries.FISSION_FUEL.values()) {
            if (fe.base().isSpecial()) continue;
            FuelDef def = fe.variantDef("_tr");
            Item fuel = fe.fuelItems().get("_tr").get();
            Item depleted = fe.depletedItems().get("_tr").get();
            if (fuel == null || depleted == null) continue;

            int time = (int) Math.round(200 * def.timeModifier);
            String name = fe.group + "_" + fe.name.replace('-', '_') + "_tr";

            UniversalProcessorRecipeBuilder.controller("msr_controller")
                    .itemInput(fuel)
                    .itemOutput(depleted)
                    .processTime(time)
                    .energyPerTick(0)
                    .save(out, name);
        }
    }
}
