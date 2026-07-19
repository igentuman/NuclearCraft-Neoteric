package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.dust;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.ingot;

/** Generates the kugelblitz chamber transmutation recipe pool. */
public class KugelblitzChamberRecipes {

    private static final String[] TAGS = {
            "ingots/iron", "ingots/gold", "ingots/copper", "ingots/netherite",
            "ingots/tin", "ingots/lead", "ingots/silver", "ingots/aluminum",
            "ingots/uranium", "ingots/steel", "ingots/bronze",
            "gems/diamond", "gems/emerald", "gems/lapis", "gems/ruby",
            "dusts/redstone", "dusts/glowstone", "dusts/coal",
            "dyes/blue", "dyes/white", "dyes/red", "dyes/green", "dyes/yellow"
    };

    private static final Item[] ITEMS = {
            Items.NETHER_STAR, Items.ENDER_EYE, Items.ENDER_PEARL, Items.DRAGON_BREATH
    };

    public static void generate(RecipeOutput out) {
        Item toughAlloy = ingot("tough_alloy");
        Item bscco = dust("bscco");
        if (toughAlloy != null && bscco != null) {
            emit(out, "tough_alloy_bscco",
                    SizedIngredient.of(toughAlloy, 1), ItemOutput.of(bscco, 2));
        }
        for (String path : TAGS) {
            TagKey<Item> tag = tag(path);
            emit(out, path.replace('/', '_'),
                    SizedIngredient.of(tag, 1), ItemOutput.of(tag, 1));
        }
        for (Item item : ITEMS) {
            String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
            emit(out, name, SizedIngredient.of(item, 1), ItemOutput.of(item, 1));
        }
    }

    private static void emit(RecipeOutput out, String name, SizedIngredient input, ItemOutput output) {
        KugelblitzRecipe recipe = new KugelblitzRecipe(input, output, 1.0, 1.0);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "kugelblitz_chamber/" + name), recipe, null);
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
