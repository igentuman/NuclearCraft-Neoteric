package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates crystallizer recipes that solidify fluids into dusts, gems, and crystal items. */
public class CrystallizerRecipes {

    public static void crystallizer(RecipeOutput out) {
        crystal(out, "salt",             Fluids.WATER,                   10000, prt("salt", 1), 500, 125);
        crystal(out, "redstone",         fluidOf("redstone"),            MOLTEN_INGOT, van(Items.REDSTONE, 1), 100, 25);
        crystal(out, "glowstone",        fluidOf("glowstone"),           MOLTEN_INGOT, van(Items.GLOWSTONE_DUST, 1), 100, 25);
        crystal(out, "lapis",            fluidOf("lapis"),               MOLTEN_INGOT, van(Items.LAPIS_LAZULI, 1));
        crystal(out, "sulfur",           fluidOf("sulfur"),              MOLTEN_INGOT, dst("sulfur", 1));
        crystal(out, "boron_nitride",    fluidOf("boron_nitride_solution"), 250, dst("boron_nitride", 1));
        crystal(out, "yellowcake",       fluidOf("uranium_oxide"),       MOLTEN_INGOT, dst("yellowcake", 1));
        crystal(out, "potassium_iodide", fluidOf("potassium_iodide"),    MOLTEN_INGOT, dst("potassium_iodide", 1));
        crystal(out, "fluorite",         fluidOf("fluorite_water"),      250, dst("fluorite", 1));
        crystal(out, "calcium_sulfate",  fluidOf("calcium_sulfate_solution"), 250, dst("calcium_sulfate", 1));
        crystal(out, "sodium_fluoride",  fluidOf("sodium_fluoride_solution"), 250, dst("sodium_fluoride", 1));
        crystal(out, "potassium_fluoride", fluidOf("potassium_fluoride_solution"), 250, dst("potassium_fluoride", 1));
        crystal(out, "sodium_hydroxide", fluidOf("sodium_hydroxide_solution"), 250, dst("sodium_hydroxide", 1), 100, 25);
        crystal(out, "potassium_hydroxide", fluidOf("potassium_hydroxide_solution"), MOLTEN_INGOT, dst("potassium_hydroxide", 1), 100, 25);
        crystal(out, "borax",            fluidOf("borax_solution"),      250, dst("borax", 1), 100, 25);
        crystal(out, "irradiated_borax", fluidOf("irradiated_borax_solution"), 250, dst("irradiated_borax", 1), 100, 25);
        crystal(out, "polonium",         fluidOf("polonium"),            1000, i(modItem("mekanism:pellet_polonium"), 1), 600);
    }
}
