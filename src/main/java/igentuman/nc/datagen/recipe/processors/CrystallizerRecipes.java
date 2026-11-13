package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.datagen.recipe.ModRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates crystallizer recipes that solidify fluids into dusts, gems, and crystal items. */
public class CrystallizerRecipes {

    public static void crystallizer(RecipeOutput out) {
        crystal(out, "salt",             Fluids.WATER,                   10000, ModRecipeProvider.part("salt", 1), 500, 125);
        crystal(out, "redstone",         fluidOf("redstone"),            MOLTEN_INGOT, van(Items.REDSTONE, 1), 100, 25);
        crystal(out, "glowstone",        fluidOf("glowstone"),           MOLTEN_INGOT, van(Items.GLOWSTONE_DUST, 1), 100, 25);
        crystal(out, "lapis",            fluidOf("lapis"),               MOLTEN_INGOT, van(Items.LAPIS_LAZULI, 1));
        crystal(out, "sulfur",           fluidOf("sulfur"),              MOLTEN_INGOT, ModRecipeProvider.dust("sulfur", 1));
        crystal(out, "boron_nitride",    fluidOf("boron_nitride_solution"), 250, ModRecipeProvider.dust("boron_nitride", 1));
        crystal(out, "yellowcake",       fluidOf("uranium_oxide"),       MOLTEN_INGOT, ModRecipeProvider.dust("yellowcake", 1));
        crystal(out, "potassium_iodide", fluidOf("potassium_iodide"),    MOLTEN_INGOT, ModRecipeProvider.dust("potassium_iodide", 1));
        crystal(out, "fluorite",         fluidOf("fluorite_water"),      250, ModRecipeProvider.dust("fluorite", 1));
        crystal(out, "calcium_sulfate",  fluidOf("calcium_sulfate_solution"), 250, ModRecipeProvider.dust("calcium_sulfate", 1));
        crystal(out, "sodium_fluoride",  fluidOf("sodium_fluoride_solution"), 250, ModRecipeProvider.dust("sodium_fluoride", 1));
        crystal(out, "potassium_fluoride", fluidOf("potassium_fluoride_solution"), 250, ModRecipeProvider.dust("potassium_fluoride", 1));
        crystal(out, "sodium_hydroxide", fluidOf("sodium_hydroxide_solution"), 250, ModRecipeProvider.dust("sodium_hydroxide", 1), 100, 25);
        crystal(out, "potassium_hydroxide", fluidOf("potassium_hydroxide_solution"), MOLTEN_INGOT, ModRecipeProvider.dust("potassium_hydroxide", 1), 100, 25);
        crystal(out, "borax",            fluidOf("borax_solution"),      250, ModRecipeProvider.dust("borax", 1), 100, 25);
        crystal(out, "irradiated_borax", fluidOf("irradiated_borax_solution"), 250, ModRecipeProvider.dust("irradiated_borax", 1), 100, 25);
        crystal(out, "polonium",         fluidOf("polonium"),            1000, i(modItem("mekanism:pellet_polonium"), 1), 600);

        for (String material : LeacherRecipes.SLURRY_MATERIALS) {
            crystal(out, material + "_clean_slurry", fluidOf(material + "_clean_slurry"), 400, ModRecipeProvider.dust(material, 2));
        }
    }
}
