package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class OreVeinsRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        OreVeinsRecipes.consumer = consumer;
        ID = "nc_ore_veins";

        add(
            new HashMap<String, Integer>() {
                {
                    put("uranium", 70);
                    put("thorium", 20);
                    put("lead", 7);
                    put("silver", 3);
                }
            },
            "uraninite",
            1D, 1D, 2D
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("copper", 70);
                    put("iron", 30);
                }
            },
            "bornite"
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("platinum", 70);
                    put("gold", 30);
                }
            },
            "platinum",
                1D, 1D, 1D, 2D
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("cobalt", 99);
                    put("nickel", 1);
                }
            },
            "cobaltite"
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("lithium", 95);
                    put("aluminum", 5);
                }
            },
            "spodumene"
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("magnesium", 95);
                }
            },
            "magnesite"
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("lead", 10);
                    put("zinc", 90);
                }
            },
            "sphalerite"
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("lead", 10);
                    put("tin", 90);
                }
            },
            "cassiterite"
        );

        add(
            new HashMap<String, Integer>() {
                {
                    put("boron", 90);
                    put("sodium", 10);
                }
            },
            "borax"
        );
    }

    public static void add(HashMap<String, Integer> materials, String name, double... modifiers) {
        List<NcIngredient> ores = new ArrayList<>();
        for(String material : materials.keySet()) {
            ores.add(oreIngredient(material, materials.get(material)));
        }
        ItemStack paper = new ItemStack(NC_PARTS.get("research_paper").get(), 1);
        paper.getOrCreateTag().putString("vein", "nc.ore_vein." + name);
        oreVein(ores, NcIngredient.stack(paper), name, modifiers);
    }
}
