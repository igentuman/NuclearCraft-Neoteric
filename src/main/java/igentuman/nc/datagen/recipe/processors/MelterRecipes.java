package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.MELTER;

public class MelterRecipes {

    public static void melter(RecipeOutput out) {
        String id = MELTER;
        for (MaterialEntry mat : materials()) {
            if (!mat.hasFluid()) continue;
            Fluid fluid = mat.materialFluid().source().get();
            if (mat.hasIngot())  melt(out, id, mat.name + "_ingot", mat.ingot().get(), 1, fluid, MOLTEN_INGOT);
            if (mat.hasDust())   melt(out, id, mat.name + "_dust",  mat.dust().get(),  1, fluid, MOLTEN_INGOT);
            if (mat.hasGem())    melt(out, id, mat.name + "_gem",   mat.gem().get(),   1, fluid, MOLTEN_INGOT);
            if (mat.hasOre())    melt(out, id, mat.name + "_ore",   mat.oreItem().get(), 1, fluid, MOLTEN_INGOT * 2 + MOLTEN_NUGGET * 6);
            if (mat.hasRawOre()) melt(out, id, mat.name + "_raw",   mat.rawOre().get(), 1, fluid, MOLTEN_INGOT + MOLTEN_NUGGET * 3);
        }

        melt(out, id, "iron_ingot",   Items.IRON_INGOT,   1, fluidOf("iron"),   MOLTEN_INGOT);
        melt(out, id, "copper_ingot", Items.COPPER_INGOT, 1, fluidOf("copper"), MOLTEN_INGOT);
        melt(out, id, "gold_ingot",   Items.GOLD_INGOT,   1, fluidOf("gold"),   MOLTEN_INGOT);
        melt(out, id, "obsidian_block", Items.OBSIDIAN,   1, fluidOf("obsidian"), MOLTEN_INGOT * 2, 400, 100);
        melt(out, id, "tnt",          Items.TNT,          1, fluidOf("tnt"),    1000);
        melt(out, id, "redstone",     Items.REDSTONE,     1, fluidOf("redstone"),  MOLTEN_INGOT);
        melt(out, id, "glowstone",    Items.GLOWSTONE_DUST, 1, fluidOf("glowstone"), MOLTEN_INGOT);

        for (IsotopeEntry iso : ModEntries.ISOTOPES.values()) {
            for (String v : iso.variants().keySet()) {
                melt(out, id, "isotope_" + iso.itemId + v, iso.variants().get(v).get(), 1, iso.fluid(v), MOLTEN_INGOT);
            }
        }
        for (FuelEntry fe : ModEntries.FUELS.values()) {
            for (String v : FuelDef.FLUID_VARIANTS) {
                var fuelItem = fe.fuelItems().get(v);
                var depItem = fe.depletedItems().get(v);
                if (fuelItem != null) melt(out, id, fe.idStem + v, fuelItem.get(), 1, fe.fuelFluid(v), MOLTEN_INGOT);
                if (depItem != null)  melt(out, id, "depleted_" + fe.idStem + v, depItem.get(), 1, fe.depletedFluid(v), MOLTEN_INGOT);
            }
        }
    }
}
