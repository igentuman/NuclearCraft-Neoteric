package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;

public class TConstructAlloyingRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        TConstructAlloyingRecipes.consumer = consumer;
        ID = "alloy";


        add(
                List.of(
                        fluidIngredient(Materials.arsenic, 333),
                        fluidIngredient(Materials.boron, MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient(Materials.boron_arsenide, MOLTEN_INGOT*2)
                ), 500
        );

        add(
                List.of(
                        fluidIngredient("hydrated_gelatin", MOLTEN_INGOT),
                        fluidIngredient("sugar", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("marshmallow", MOLTEN_INGOT)
                ), 200
        );

        add(
                List.of(
                        fluidIngredient("chocolate_liquor", MOLTEN_INGOT/2),
                        fluidIngredient("cocoa_butter", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT)
                ), 200
        );

        add(
                List.of(
                        fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT),
                        fluidIngredient("sugar", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("dark_chocolate", MOLTEN_INGOT)
                ), 200
        );

        add(
                List.of(
                        fluidIngredient("dark_chocolate", MOLTEN_INGOT),
                        fluidIngredient("pasteurized_milk", 250)
                ),
                List.of(
                        fluidIngredient("milk_chocolate", MOLTEN_INGOT*2)
                ), 200
        );

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                if(name.contains("mixed")) {
                    continue;
                }
                fuelMixRecipe(name, subType,
                        FuelManager.all().get(name).get(subType).getDefault().isotopes[0] + "",
                        FuelManager.all().get(name).get(subType).getDefault().isotopes[1] + ""
                );

                fuelMixRecipe(name, subType + "_ox",
                        FuelManager.all().get(name).get(subType).getOxide().isotopes[0] + "_ox",
                        FuelManager.all().get(name).get(subType).getOxide().isotopes[1] + "_ox");

                fuelMixRecipe(name, subType + "_ni",
                        FuelManager.all().get(name).get(subType).getNitride().isotopes[0] + "_ni",
                        FuelManager.all().get(name).get(subType).getNitride().isotopes[1] + "_ni");

                fuelMixRecipe(name, subType + "_za",
                        FuelManager.all().get(name).get(subType).getZirconiumAlloy().isotopes[0] + "_za",
                        FuelManager.all().get(name).get(subType).getZirconiumAlloy().isotopes[1] + "_za");
            }
        }
    }

    private static void fuelMixRecipe(String name, String type, String isotope1, String isotope2) {
        int isotope1Amount = MOLTEN_INGOT/3;
        int isotope2Amount = MOLTEN_INGOT*3;
        if(type.startsWith("he")) {
            isotope1Amount = MOLTEN_INGOT;
            isotope2Amount = MOLTEN_INGOT*2;
        }
        tconstructAlloy(List.of(fluidIngredient(name+"/"+isotope1, isotope1Amount), fluidIngredient(name+"/"+isotope2, isotope2Amount)), List.of(fluidIngredient("fuel_"+name+"_"+type.replace("-","_"), MOLTEN_INGOT)), true, 800);
    }

    protected static void add(List<FluidStackIngredient> input, List<FluidStackIngredient> output, int temperature) {
        tconstructAlloy(input, output, false, temperature);
    }
}
