package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.INGOT_FORMER;

/** Generates ingot former recipes that cast molten fluids into ingots, gems, isotopes, and fuel pellets. */
public class IngotFormerRecipes {

    public static void ingotFormer(RecipeOutput out) {
        String id = INGOT_FORMER;
        for (MaterialEntry mat : materials()) {
            if (!(mat.hasIngot() && mat.hasFluid())) continue;
            f2i(out, id, mat.name, mat.materialFluid().source().get(), MOLTEN_INGOT, mat.ingot().get(), 1);
        }

        f2i(out, id, "copper", fluidOf("copper"), MOLTEN_INGOT, Items.COPPER_INGOT, 1);
        f2i(out, id, "iron",   fluidOf("iron"),   MOLTEN_INGOT, Items.IRON_INGOT,   1);
        f2i(out, id, "gold",   fluidOf("gold"),   MOLTEN_INGOT, Items.GOLD_INGOT,   1);
        f2i(out, id, "boron_arsenide", fluidOf("boron_arsenide"), MOLTEN_INGOT, gem("boron_arsenide"), 1);
        f2i(out, id, "obsidian", fluidOf("obsidian"), MOLTEN_INGOT * 2, Items.OBSIDIAN, 1, 400, 100);

        f2i(out, id, "ground_cocoa_nibs", fluidOf("chocolate_liquor"), MOLTEN_INGOT, part("ground_cocoa_nibs"), 1, 50, 25);
        for (String food : new String[]{"cocoa_butter", "unsweetened_chocolate", "dark_chocolate", "milk_chocolate"}) {
            f2i(out, id, food, fluidOf(food), MOLTEN_INGOT, part(food), 1, 50, 25);
        }
        for (String food : new String[]{"gelatin", "marshmallow"}) {
            f2i(out, id, food, fluidOf(food), MOLTEN_INGOT, part(food), 1, 100, 25);
        }

        for (IsotopeEntry iso : ModEntries.ISOTOPES.values()) {
            for (String v : iso.variants().keySet()) {
                f2i(out, id, "isotope_" + iso.itemId + v, iso.fluid(v), MOLTEN_INGOT, iso.variants().get(v).get(), 1);
            }
        }
        for (FissionFuelEntry fe : ModEntries.FISSION_FUEL.values()) {
            for (String v : FuelDef.FLUID_VARIANTS) {
                var fuelItem = fe.fuelItems().get(v);
                var depItem = fe.depletedItems().get(v);
                if (fuelItem != null) f2i(out, id, fe.idStem + v, fe.fuelFluid(v), MOLTEN_INGOT, fuelItem.get(), 1);
                if (depItem != null)  f2i(out, id, "depleted_" + fe.idStem + v, fe.depletedFluid(v), MOLTEN_INGOT, depItem.get(), 1);
            }
        }
    }
}
