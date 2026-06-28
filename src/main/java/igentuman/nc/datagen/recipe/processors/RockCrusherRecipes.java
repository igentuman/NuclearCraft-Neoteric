package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

public class RockCrusherRecipes {

    public static void rockCrusher(RecipeOutput out) {
        crush(out, "kumanderite",   blockItem("kumanderite"), 2,
                iso("xenorium/298", 12), iso("quantite", 16), iso("californium/252", 8));
        crush(out, "gravel",        Items.GRAVEL,        4, van(Items.SAND, 1),         gm("silicon",      1));
        crush(out, "cobalt_ore",    oreItem("cobalt"),   4, dst("cobalt",    3),         dst("osmium",      1));
        crush(out, "amethyst",      Items.AMETHYST_BLOCK, 32, dst("yttrium", 1),         dst("hafnium",     1));
        crush(out, "end_stone",     Items.END_STONE,     3, dst("borax",     1),         dst("germanium",   1));
        crush(out, "purpur",        Items.PURPUR_BLOCK,  12, dst("purpur",   4),         dst("iridium",     1));
        crush(out, "netherrack",    Items.NETHERRACK,    2, dst("sulfur",    1));
        crush(out, "basalt",        Items.BASALT,        7, dst("tungsten",  1),         dst("niobium",     1));
        crush(out, "ancient_debris", Items.ANCIENT_DEBRIS, 1, van(Items.NETHERITE_SCRAP, 2), dst("titanium", 1));
        crush(out, "granite",       Items.GRANITE,       4, dst("rhodochrosite", 2),     dst("villiaumite", 1));
        crush(out, "diorite",       Items.DIORITE,       4, dst("zirconium", 2),         gm("fluorite",     1), gm("carobbiite", 1));
        crush(out, "andesite",      Items.ANDESITE,      4, dst("beryllium", 2),         dst("arsenic",     1));
        crush(out, "deepslate",     Items.DEEPSLATE,     7, dst("iodine",    1),         dst("obsidian",    1));
        crush(out, "tuff",          Items.TUFF,          12, dst("chromium", 1),          dst("coal",        1));
        crush(out, "calcite",       Items.CALCITE,       5, dst("calcium",   2),          dst("potassium",   1));
    }
}
