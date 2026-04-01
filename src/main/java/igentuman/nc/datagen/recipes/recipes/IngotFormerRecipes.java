package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.NCMaterial;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static igentuman.nc.setup.registration.FissionFuel.*;
import static net.minecraft.world.item.Items.*;

public class IngotFormerRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        IngotFormerRecipes.consumer = consumer;
        ID = Processors.INGOT_FORMER;
        for(String name: Materials.all().keySet()) {
            NCMaterial material = Materials.all().get(name);
            if(material.fluid && !material.isGas && material.ingot) {
                add(ingotIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT));
            }
        }

        for (String name: Materials.isotopes()) {
            for(String type: new String[] {"", "_ox", "_ni", "_za"}) {
                String key = name+type;
                if(!NC_ISOTOPES.containsKey(key)) {
                    continue;
                }
                add(ingredient(NC_ISOTOPES.get(key).get()), fluidIngredient(key, MOLTEN_INGOT));
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
                    add(ingredient(NC_FUEL.get(key).get()), fluidStackIngredient(keyStr, MOLTEN_INGOT));
                    if(name.matches("xenorium.*|quantite.*")) break;
                }
            }
        }

        add(ingredient(COPPER_INGOT), fluidIngredient("molten_"+Materials.copper, MOLTEN_INGOT));
        add(ingredient(IRON_INGOT), fluidIngredient("molten_"+Materials.iron, MOLTEN_INGOT));
        add(ingredient(GOLD_INGOT), fluidIngredient("molten_"+Materials.gold, MOLTEN_INGOT));

        add(gemStack(Materials.boron_arsenide), fluidIngredient("molten_"+Materials.boron_arsenide, MOLTEN_INGOT));
        add(ingredient(OBSIDIAN), fluidIngredient("molten_"+Materials.obsidian, MOLTEN_INGOT*2), 2D, 2D);

        add(ingredient(NCItems.NC_ITEMS.get("ground_cocoa_nibs").get()), fluidIngredient("chocolate_liquor", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("cocoa_butter").get()), fluidIngredient("cocoa_butter", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("unsweetened_chocolate").get()), fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("dark_chocolate").get()), fluidIngredient("dark_chocolate", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("milk_chocolate").get()), fluidIngredient("milk_chocolate", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("gelatin").get()), fluidIngredient("gelatin", MOLTEN_INGOT), 0.5D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("marshmallow").get()), fluidIngredient("marshmallow", MOLTEN_INGOT), 0.5D, 0.5D);
    }

    protected static void add(NcIngredient outputItem, FluidStack inputFluid, double...modifiers) {
        itemsAndFluids(new ArrayList<>(), List.of(outputItem), List.of(IngredientCreatorAccess.fluid().from(inputFluid)), new ArrayList<>(), modifiers);
    }
    protected static void add(NcIngredient outputItem, FluidStackIngredient inputFluid, double...modifiers) {
        itemsAndFluids(new ArrayList<>(), List.of(outputItem), List.of(inputFluid), new ArrayList<>(), modifiers);
    }
}
