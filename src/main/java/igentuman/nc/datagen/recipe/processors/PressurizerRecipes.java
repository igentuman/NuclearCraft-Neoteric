package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.MaterialEntry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.PRESSURIZER;

public class PressurizerRecipes {

    public static void pressurizer(RecipeOutput out) {
        String id = PRESSURIZER;
        for (MaterialEntry mat : materials()) {
            if (!mat.hasPlate()) continue;
            Item input = mat.hasIngot() ? mat.ingot().get() : (mat.hasDust() ? mat.dust().get() : null);
            if (input == null) continue;
            rec(out, id, mat.name, new I[]{i(input, 1)}, NF, new I[]{i(mat.plate().get(), 1)}, NF);
        }

        i2i(out, id, "iron_ingot",   Items.IRON_INGOT,   plate("iron"),   1);
        i2i(out, id, "copper_ingot", Items.COPPER_INGOT, plate("copper"), 1);
        i2i(out, id, "netherite_ingot", Items.NETHERITE_INGOT, plate("netherite"), 1);
        i2i(out, id, "graphite_dust", dust("graphite"), plate("graphite"), 1);
        i2i(out, id, "pyrolitic_carbon", ingot("graphite"), ingot("pyrolitic_carbon"), 1);

        i2i(out, id, "diamond",       dust("diamond"),       Items.DIAMOND,      1);
        i2i(out, id, "quartz",        dust("quartz"),        Items.QUARTZ,       1);
        i2i(out, id, "lapis",         dust("lapis"),         Items.LAPIS_LAZULI, 1);
        i2i(out, id, "emerald",       dust("emerald"),       Items.EMERALD,      1);
        i2i(out, id, "rhodochrosite", dust("rhodochrosite"), gem("rhodochrosite"), 1);
        i2i(out, id, "boron_nitride", dust("boron_nitride"), gem("boron_nitride"), 1);
        i2i(out, id, "fluorite",      dust("fluorite"),      gem("fluorite"),    1);
        i2i(out, id, "villiaumite",   dust("villiaumite"),   gem("villiaumite"), 1);
        i2i(out, id, "carobbiite",    dust("carobbiite"),    gem("carobbiite"),  1);

        rec(out, id, "obsidian", new I[]{i(dust("obsidian"), 4)}, NF, new I[]{van(Items.OBSIDIAN, 1)}, NF);
        rec(out, id, "silicon_boule", new I[]{i(ingot("silicon_carbide"), 3)}, NF, new I[]{prt("silicon_boule", 1)}, NF);

        rec(out, id, "americium241",  new I[]{i(isotope("americium/241"),  9)}, NF, new I[]{i(blockItem("americium241"),  1)}, NF);
        rec(out, id, "uranium238",    new I[]{i(isotope("uranium/238"),    9)}, NF, new I[]{i(blockItem("uranium238"),    1)}, NF);
        rec(out, id, "californium250", new I[]{i(isotope("californium/250"), 9)}, NF, new I[]{i(blockItem("californium250"), 1)}, NF);
        rec(out, id, "plutonium238",  new I[]{i(isotope("plutonium/238"),  9)}, NF, new I[]{i(blockItem("plutonium238"),  1)}, NF);
    }
}
