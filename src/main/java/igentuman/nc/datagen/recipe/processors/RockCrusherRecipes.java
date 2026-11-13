package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.datagen.recipe.ModRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates rock crusher recipes that break stones and ores into dusts and gems. */
public class RockCrusherRecipes {

    public static void rockCrusher(RecipeOutput out) {
        crush(out, "kumanderite",   blockItem("kumanderite"), 2,
                new I[]{ModRecipeProvider.isotope("xenorium/298", 12), ModRecipeProvider.isotope("quantite", 16), ModRecipeProvider.isotope("californium/252", 8)}, 2000, 100);
        crush(out, "gravel",        Items.GRAVEL,        4, new I[]{van(Items.SAND, 1),         ModRecipeProvider.gem("silicon",      1)});
        crush(out, "cobalt_ore",    oreItem("cobalt"),   4, new I[]{ModRecipeProvider.dust("cobalt",    3),         ModRecipeProvider.dust("osmium",      1)});
        crush(out, "amethyst",      Items.AMETHYST_BLOCK, 32, new I[]{ModRecipeProvider.dust("yttrium", 1),         ModRecipeProvider.dust("hafnium",     1)});
        crush(out, "end_stone",     Items.END_STONE,     3, new I[]{ModRecipeProvider.dust("borax",     1),         ModRecipeProvider.dust("germanium",   1)});
        crush(out, "purpur",        Items.PURPUR_BLOCK,  12, new I[]{ModRecipeProvider.dust("purpur",   4),         ModRecipeProvider.dust("iridium",     1)});
        crush(out, "netherrack",    Items.NETHERRACK,    2, new I[]{ModRecipeProvider.dust("sulfur",    1)});
        crush(out, "basalt",        Items.BASALT,        7, new I[]{ModRecipeProvider.dust("tungsten",  1),         ModRecipeProvider.dust("niobium",     1)});
        crush(out, "ancient_debris", Items.ANCIENT_DEBRIS, 1, new I[]{van(Items.NETHERITE_SCRAP, 2), ModRecipeProvider.dust("titanium", 1)});
        crush(out, "granite",       Items.GRANITE,       4, new I[]{ModRecipeProvider.dust("rhodochrosite", 2),     ModRecipeProvider.dust("villiaumite", 1)});
        crush(out, "diorite",       Items.DIORITE,       4, new I[]{ModRecipeProvider.dust("zirconium", 2),         ModRecipeProvider.gem("fluorite",     1), ModRecipeProvider.gem("carobbiite", 1)});
        crush(out, "andesite",      Items.ANDESITE,      4, new I[]{ModRecipeProvider.dust("beryllium", 2),         ModRecipeProvider.dust("arsenic",     1)});
        crush(out, "deepslate",     Items.DEEPSLATE,     7, new I[]{ModRecipeProvider.dust("iodine",    1),         ModRecipeProvider.dust("obsidian",    1)});
        crush(out, "tuff",          Items.TUFF,          12, new I[]{ModRecipeProvider.dust("chromium", 1),          ModRecipeProvider.dust("coal",        1)});
        crush(out, "calcite",       Items.CALCITE,       5, new I[]{ModRecipeProvider.dust("calcium",   2),          ModRecipeProvider.dust("potassium",   1)});
    }
}
