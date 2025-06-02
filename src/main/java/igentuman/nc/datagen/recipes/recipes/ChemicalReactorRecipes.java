package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static net.minecraft.world.level.material.Fluids.WATER;

public class ChemicalReactorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ChemicalReactorRecipes.consumer = consumer;
        ID = Processors.CHEMICAL_REACTOR;

        add(
                List.of(
                        fluidIngredient(Materials.arsenic, 333),
                        fluidIngredient(Materials.boron, MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient(Materials.boron_arsenide, MOLTEN_INGOT*2)
                ), 0.5D, 1.2D
        );

        add(
                List.of(
                        fluidIngredient("ammonia", 350),
                        fluidIngredient("oxygen", 650)
                ),
                List.of(
                        fluidIngredient("nitric_oxide", 750),
                        fluidIngredient("minecraft:water", 250)
                ), 0.5D, 0.6D
        );

        add(
                List.of(
                        fluidIngredient("nitric_oxide", 500),
                        fluidIngredient("oxygen", 250)
                ),
                List.of(
                        fluidIngredient("nitrogen_dioxide", 750)
                ), 0.5D, 0.6D
        );

        add(
                List.of(
                        fluidIngredient("nitrogen_dioxide", 750),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidIngredient("nitric_acid", 1000)
                ), 1.5D, 0.4D
        );

        add(
                List.of(
                        fluidIngredient("nitric_acid", 250),
                        fluidIngredient("hydrochloric_acid", 750)
                ),
                List.of(
                        fluidIngredient("aqua_regia_acid", 1000)
                ), 1.5D
        );

        add(
                List.of(
                        fluidIngredient("hydrogen_chloride", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidIngredient("hydrochloric_acid", 500)
                )
        );

        add(
                List.of(
                    fluidIngredient("boron", MOLTEN_INGOT),
                    fluidIngredient("hydrogen", 666)
                        ),
                List.of(
                        fluidIngredient("diborane", 500)
                ), 0.5D
        );

        add(
                List.of(
                    fluidIngredient("diborane", 250),
                    fluidIngredient("minecraft:water", 750)
                        ),
                List.of(
                        fluidIngredient("boric_acid", 500),
                        fluidIngredient("hydrogen", 500)
                ), 0.5D
        );

        add(
                List.of(
                    fluidIngredient("boric_acid", 500),
                    fluidIngredient("ammonia", 500)
                        ),
                List.of(
                        fluidIngredient("boron_nitride_solution", MOLTEN_INGOT/2),
                        fluidIngredient("minecraft:water", 1000)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("nitrogen", 250),
                        fluidIngredient("hydrogen", 750)
                ),
                List.of(
                        fluidIngredient("ammonia", 750)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("hydrogen", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                List.of(
                        fluidIngredient("minecraft:water", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("deuterium", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                List.of(
                        fluidIngredient("heavy_water", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("lithium", MOLTEN_INGOT*2),
                        fluidIngredient("fluorite_water", 250)
                ),
                List.of(
                        fluidIngredient("lithium_fluoride", 360)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("beryllium", MOLTEN_INGOT*2),
                        fluidIngredient("fluorite_water", 250)
                ),
                List.of(
                        fluidIngredient("beryllium_fluoride", 360)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur", MOLTEN_INGOT/2),
                        fluidIngredient("liquid_oxygen", 500)
                ),
                List.of(
                        fluidIngredient("sulfur_dioxide", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur_dioxide", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                List.of(
                        fluidIngredient("sulfur_trioxide", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur_trioxide", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidIngredient("sulfuric_acid", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluorite_water", 500),
                        fluidIngredient("sulfuric_acid", 500)
                ),
                List.of(
                        fluidIngredient("hydrofluoric_acid", 1000),
                        fluidIngredient("calcium_sulfate_solution", 50)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sodium_fluoride_solution", MOLTEN_INGOT/2),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidIngredient("sodium_hydroxide_solution", MOLTEN_INGOT/2),
                        fluidIngredient("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("potassium_fluoride_solution", MOLTEN_INGOT/2),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidIngredient("potassium_hydroxide_solution", MOLTEN_INGOT/2),
                        fluidIngredient("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sodium_fluoride_solution", MOLTEN_INGOT),
                        fluidIngredient("boric_acid", 2000)
                ),
                List.of(
                        fluidIngredient("borax_solution", MOLTEN_INGOT/2),
                        fluidIngredient("hydrofluoric_acid", 1500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("oxygen_difluoride", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidIngredient("liquid_oxygen", 250),
                        fluidIngredient("hydrofluoric_acid", 250)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("oxygen_difluoride", 250),
                        fluidIngredient("sulfur_dioxide", 250)
                ),
                List.of(
                        fluidIngredient("sulfur_trioxide", 250),
                        fluidIngredient("fluorite_water", 250)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("liquid_oxygen", 250),
                        fluidIngredient("fluorite_water", 500)
                ),
                List.of(
                        fluidIngredient("oxygen_difluoride", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("manganese_dioxide", MOLTEN_INGOT/2),
                        fluidIngredient("carbon", MOLTEN_INGOT)
                ),
                List.of(
                        fluidIngredient("manganese", MOLTEN_INGOT/2),
                        fluidIngredient("carbon_monoxide", 750)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sugar", MOLTEN_INGOT/2),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidIngredient("ethanol", 2000),
                        fluidIngredient("carbon_dioxide", 1000)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("carbon_dioxide", 250),
                        fluidIngredient("hydrogen", 250)
                ),
                List.of(
                        fluidIngredient("carbon_monoxide", 250),
                        fluidIngredient("minecraft:water", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("carbon_monoxide", 250),
                        fluidIngredient("hydrogen", 500)
                ),
                List.of(
                        fluidIngredient("methanol", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("methanol", 250),
                        fluidIngredient("hydrofluoric_acid", 250)
                ),
                List.of(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("minecraft:water", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("sodium_hydroxide_solution", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("ethene", 250),
                        fluidIngredient("sodium_fluoride_solution", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("potassium_hydroxide_solution", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("ethene", 250),
                        fluidIngredient("potassium_fluoride_solution", 250)
                ), 0.5D
        );

        add(
                List.of(
                        fluidIngredient("ethene", 250),
                        fluidIngredient("sulfuric_acid", 250)
                ),
                List.of(
                        fluidIngredient("ethanol", 250),
                        fluidIngredient("sulfur_trioxide", 250)
                ), 0.5D
        );

        add(
                List.of(
                        fluidIngredient("ice", 350),
                        fluidIngredient("ethanol", 150)
                ),
                List.of(
                        fluidIngredient("slurry_ice", 500)
                )
        );

        add(
                List.of(
                        fluidIngredient("boron_arsenide", MOLTEN_INGOT*2),
                        fluidIngredient("minecraft:water", 100)
                ),
                List.of(
                        fluidIngredient("boron_arsenide_solution", 360)
                )
        );

        add(
                List.of(
                        fluidIngredient("hydrogen", 250),
                        fluidIngredient("chlorine", 150)
                ),
                List.of(
                        fluidIngredient("hydrogen_chloride", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("gelatin", MOLTEN_INGOT/2),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidIngredient("hydrated_gelatin", MOLTEN_INGOT*2)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("hydrated_gelatin", MOLTEN_INGOT),
                        fluidIngredient("sugar", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("marshmallow", MOLTEN_INGOT)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("chocolate_liquor", MOLTEN_INGOT/2),
                        fluidIngredient("cocoa_butter", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT),
                        fluidIngredient("sugar", MOLTEN_INGOT/2)
                ),
                List.of(
                        fluidIngredient("dark_chocolate", MOLTEN_INGOT)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("dark_chocolate", MOLTEN_INGOT),
                        fluidIngredient("pasteurized_milk", 250)
                ),
                List.of(
                        fluidIngredient("milk_chocolate", MOLTEN_INGOT*2)
                ), 0.5D, 0.5D
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
        fluidsAndFluids(List.of(fluidIngredient(name+"/"+isotope1, isotope1Amount), fluidIngredient(name+"/"+isotope2, isotope2Amount)), List.of(fluidIngredient("fuel_"+name+"_"+type.replace("-","_"), MOLTEN_INGOT)), true);
    }

    protected static void add(List<FluidStackIngredient> input, List<FluidStackIngredient> output, double...modifiers) {
        fluidsAndFluids(input, output, modifiers);
    }
}
