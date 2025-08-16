package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.materials.NCMaterial;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static igentuman.nc.setup.registration.FissionFuel.NC_FUEL;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static net.minecraft.world.item.Items.*;

public class TConstructCastingRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        TConstructCastingRecipes.consumer = consumer;
        ID = "casting_table";


        for (String name: Materials.isotopes()) {
            if(name.contains("xen")) continue;
            for(String type: new String[] {"", "_ox", "_ni", "_za"}) {
                String key = name+type;
                if(!NC_ISOTOPES.containsKey(key)) {
                    continue;
                }
                add(ingredient(NC_ISOTOPES.get(key).get()), fluidIngredient(key, MOLTEN_INGOT), 40);
            }
        }

        for (String name: FuelManager.all().keySet()) {
            if(name.contains("xen")) continue;
            for(String subType: FuelManager.all().get(name).keySet()) {
                for (String type : new String[]{"", "za", "ox","ni"}) {

                    List<String> key = List.of("fuel", name, subType, type);

                    String keyStr = "fuel_"+name +"_"+ subType;
                    if(!type.isEmpty()){
                        keyStr += "_";
                    }
                    keyStr+= type;
                    add(ingredient(NC_FUEL.get(key).get()), fluidStackIngredient(keyStr, MOLTEN_INGOT), 50);
                    if(name.matches("xenorium.*|quantite.*")) break;
                }
            }
        }


        add(gemStack(Materials.boron_arsenide), fluidStack(Materials.boron_arsenide, MOLTEN_INGOT), 57);

        add(ingredient(NCItems.NC_ITEMS.get("ground_cocoa_nibs").get()), fluidIngredient("chocolate_liquor", MOLTEN_INGOT), 20);
        add(ingredient(NCItems.NC_ITEMS.get("cocoa_butter").get()), fluidIngredient("cocoa_butter", MOLTEN_INGOT), 20);
        add(ingredient(NCItems.NC_ITEMS.get("unsweetened_chocolate").get()), fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT), 20);
        add(ingredient(NCItems.NC_FOOD.get("dark_chocolate").get()), fluidIngredient("dark_chocolate", MOLTEN_INGOT), 20);
        add(ingredient(NCItems.NC_FOOD.get("milk_chocolate").get()), fluidIngredient("milk_chocolate", MOLTEN_INGOT), 20);
        add(ingredient(NCItems.NC_ITEMS.get("gelatin").get()), fluidIngredient("gelatin", MOLTEN_INGOT), 20);
        add(ingredient(NCItems.NC_FOOD.get("marshmallow").get()), fluidIngredient("marshmallow", MOLTEN_INGOT), 20);
    }

    protected static void add(NcIngredient outputItem, FluidStack inputFluid, int coolingTime) {
        tconstructCasting(new ArrayList<>(), List.of(outputItem), List.of(IngredientCreatorAccess.fluid().from(inputFluid)), new ArrayList<>(), coolingTime);
    }
    protected static void add(NcIngredient outputItem, FluidStackIngredient inputFluid, int coolingTime) {
        tconstructCasting(new ArrayList<>(), List.of(outputItem), List.of(inputFluid), new ArrayList<>(), coolingTime);
    }
}
