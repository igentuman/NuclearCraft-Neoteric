package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.fission.FissionFuelRecipe;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import static igentuman.nc.NuclearCraft.MODID;

/** Generates fission reactor fuel recipes mapping fuel items to depleted output with time, power, and heat. */
public class FissionFuelRecipes {

    public static void fissionFuel(RecipeOutput out) {
        for (FissionFuelEntry fe : ModEntries.FISSION_FUEL.values()) {
            String[] variants = fe.base().isSpecial() ? new String[]{""} : FuelDef.ITEM_VARIANTS;
            for (String v : variants) {
                if (v.equals("_tr")) continue; // triso has no in-reactor recipe (0 power)
                FuelDef def = fe.variantDef(v);
                if (def.forgeEnergy <= 0) continue;
                Item fuel = fe.fuelItems().get(v).get();
                Item depleted = fe.depletedItems().get(v).get();
                if (fuel == null || depleted == null) continue;

                int time = (int) Math.round(def.depletion * 20 * def.timeModifier);
                int power = (int) Math.round(def.forgeEnergy * def.powerModifier);
                int heat = (int) Math.round(def.heat);

                FissionFuelRecipe recipe = new FissionFuelRecipe(
                        Ingredient.of(fuel), ItemOutput.of(depleted, 1), time, power, heat);
                String path = "fission_reactor/" + fe.group + "_" + fe.name.replace('-', '_') + v;
                out.accept(ResourceLocation.fromNamespaceAndPath(MODID, path), recipe, null);
            }
        }
    }
}
