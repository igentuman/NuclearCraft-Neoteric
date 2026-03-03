package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.materials.NCMaterial;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.List;

import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_NUGGET;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static net.minecraft.world.item.Items.*;
import static net.minecraft.world.item.Items.SUGAR;

public class TConstructMeltingRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        TConstructMeltingRecipes.consumer = consumer;
        ID = "melting";
        for(String name: List.of("tough_alloy")) {
            add(dustIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT),800, 40);
            add(ingotIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT),800, 40);
        }

        add(dustIngredient(Materials.polonium), fluidIngredient(Materials.polonium, MOLTEN_INGOT), 700, 40);
        add(dustIngredient(Materials.sulfur), fluidIngredient(Materials.sulfur, MOLTEN_INGOT), 500, 40);



        for (String name: Materials.isotopes()) {
            if(name.contains("xen")) continue;
            for(String type: new String[] {"", "_ox", "_ni", "_za"}) {
                String key = name+type;
                if(!NC_ISOTOPES.containsKey(key)) {
                    continue;
                }
                add(ingredient(NC_ISOTOPES.get(key).get()), fluidIngredient(key, MOLTEN_INGOT), 700, 30);
            }
        }

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                for (String type : new String[]{"", "za", "ox","ni"}) {

                    List<String> key = List.of("fuel", name, subType, type);

                    String keyStr = "fuel_"+name +"_"+ subType;
                    if(!type.isEmpty()){
                        keyStr += "_";
                    }
                    keyStr+= type;
                    add(ingredient(FissionFuel.NC_FUEL.get(key).get()), fluidStackIngredient(keyStr, MOLTEN_INGOT), 700, 40);

                    key = List.of("depleted", name, subType, type);
                    keyStr = "depleted_"+keyStr;
                    add(ingredient(FissionFuel.NC_DEPLETED_FUEL.get(key).get()), fluidStackIngredient(keyStr, MOLTEN_INGOT), 700, 40);
                    if(name.matches("xenorium.*|quantite.*")) break;
                }
            }
        }

        add(dustIngredient(Materials.sodium_hydroxide), fluidIngredient(Materials.sodium_hydroxide, MOLTEN_INGOT), 500, 20);
        add(dustIngredient(Materials.potassium_hydroxide), fluidIngredient(Materials.potassium_hydroxide, MOLTEN_INGOT), 500, 20);
        add(dustIngredient(Materials.arsenic), fluidIngredient(Materials.arsenic, MOLTEN_INGOT), 700, 20);
        add(gemIngredient(Materials.boron_arsenide), fluidIngredient(Materials.boron_arsenide, MOLTEN_INGOT), 700, 20);

        add(ingredient(NCItems.NC_ITEMS.get("ground_cocoa_nibs").get()), fluidIngredient("chocolate_liquor", MOLTEN_INGOT), 100, 10);
        add(ingredient(NCItems.NC_ITEMS.get("cocoa_butter").get()), fluidIngredient("cocoa_butter", MOLTEN_INGOT), 100, 10);
        add(ingredient(NCItems.NC_ITEMS.get("unsweetened_chocolate").get()), fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT), 200, 10);
        add(ingredient(NCItems.NC_FOOD.get("dark_chocolate").get()), fluidIngredient("dark_chocolate", MOLTEN_INGOT), 100, 10);
        add(ingredient(NCItems.NC_FOOD.get("milk_chocolate").get()), fluidIngredient("milk_chocolate", MOLTEN_INGOT), 100, 10);
        add(ingredient(SUGAR), fluidIngredient("sugar", MOLTEN_INGOT), 150, 10);
        add(ingredient(NCItems.NC_ITEMS.get("gelatin").get()), fluidIngredient("gelatin", MOLTEN_INGOT), 150, 10);
        add(ingredient(NCItems.NC_FOOD.get("marshmallow").get()), fluidIngredient("marshmallow", MOLTEN_INGOT), 100, 10);


    }

    protected static void add(NcIngredient input, FluidStackIngredient output, int temperature, int time) {
        tconstructMelt(List.of(input), List.of(output), false, temperature, time);
    }
}
