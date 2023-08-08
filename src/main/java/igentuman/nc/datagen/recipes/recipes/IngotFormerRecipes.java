package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.NCMaterial;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.OBSIDIAN;

public class IngotFormerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        IngotFormerRecipes.consumer = consumer;
        ID = Processors.INGOT_FORMER;
        for(String name: Materials.all().keySet()) {
            NCMaterial material = Materials.all().get(name);
            if(material.fluid && !material.isGas && material.ingot) {
                add(ingotStack(name), fluidStack(name, 144));
            }
        }

        for (String name: Materials.isotopes()) {
            for(String type: new String[] {"", "_ox", "_ni", "_za"}) {
                String key = name+type;
                add(ingredient(Fuel.NC_ISOTOPES.get(key).get()), fluidStack(key, 144));
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
                    add(ingredient(Fuel.NC_FUEL.get(key).get()), fluidStack(keyStr, 144));

                    key = List.of("depleted", name, subType, type);
                    keyStr = "depleted_"+keyStr;
                    add(ingredient(Fuel.NC_DEPLETED_FUEL.get(key).get()), fluidStack(keyStr, 144));
                }
            }
        }


        add(gemStack(Materials.boron_arsenide), fluidStack(Materials.boron_arsenide, 144));
        add(ingredient(OBSIDIAN), fluidStack(Materials.obsidian, 288), 2D, 2D);

        add(ingredient(NCItems.NC_ITEMS.get("ground_cocoa_nibs").get()), fluidStack("chocolate_liquor", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("cocoa_butter").get()), fluidStack("cocoa_butter", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("unsweetened_chocolate").get()), fluidStack("unsweetened_chocolate", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("dark_chocolate").get()), fluidStack("dark_chocolate", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("milk_chocolate").get()), fluidStack("milk_chocolate", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("gelatin").get()), fluidStack("gelatin", 144), 0.5D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("marshmallow").get()), fluidStack("marshmallow", 144), 0.5D, 0.5D);
    }

    protected static void add(NcIngredient outputItem, FluidStack inputFluid, double...modifiers) {
        itemsAndFluids(new ArrayList<>(), List.of(outputItem), List.of(IngredientCreatorAccess.fluid().from(inputFluid)), new ArrayList<>(), modifiers);
    }
}
