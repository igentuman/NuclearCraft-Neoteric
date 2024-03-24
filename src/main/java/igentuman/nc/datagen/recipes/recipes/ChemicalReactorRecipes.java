package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.level.material.Fluids.WATER;

public class ChemicalReactorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ChemicalReactorRecipes.consumer = consumer;
        ID = Processors.CHEMICAL_REACTOR;

        add(
                List.of(
                        fluidIngredient(Materials.arsenic, 333),
                        fluidIngredient(Materials.boron, 72)
                ),
                List.of(
                        fluidIngredient(Materials.boron_arsenide, 288)
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
                    fluidIngredient("boron", 144),
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
                        fluidIngredient("boron_nitride_solution", 72),
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
                        fluidIngredient("lithium", 288),
                        fluidIngredient("fluorite_water", 250)
                ),
                List.of(
                        fluidIngredient("lithium_fluoride", 360)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("beryllium", 288),
                        fluidIngredient("fluorite_water", 250)
                ),
                List.of(
                        fluidIngredient("beryllium_fluoride", 360)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur", 72),
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
                        fluidIngredient("sodium_fluoride_solution", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidIngredient("sodium_hydroxide_solution", 72),
                        fluidIngredient("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("potassium_fluoride_solution", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidIngredient("potassium_hydroxide_solution", 72),
                        fluidIngredient("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sodium_fluoride_solution", 144),
                        fluidIngredient("boric_acid", 2000)
                ),
                List.of(
                        fluidIngredient("borax_solution", 72),
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
                        fluidIngredient("manganese_dioxide", 72),
                        fluidIngredient("carbon", 144)
                ),
                List.of(
                        fluidIngredient("manganese", 72),
                        fluidIngredient("carbon_monoxide", 750)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sugar", 72),
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
                        fluidIngredient("sodium_hydroxide_solution", 72)
                ),
                List.of(
                        fluidIngredient("ethene", 250),
                        fluidIngredient("sodium_fluoride_solution", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("potassium_hydroxide_solution", 72)
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
                        fluidIngredient("boron_arsenide", 288),
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
                        fluidIngredient("gelatin", 72),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidIngredient("hydrated_gelatin", 288)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("hydrated_gelatin", 144),
                        fluidIngredient("sugar", 72)
                ),
                List.of(
                        fluidIngredient("marshmallow", 144)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("chocolate_liquor", 72),
                        fluidIngredient("cocoa_butter", 72)
                ),
                List.of(
                        fluidIngredient("unsweetened_chocolate", 144)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("unsweetened_chocolate", 144),
                        fluidIngredient("sugar", 72)
                ),
                List.of(
                        fluidIngredient("dark_chocolate", 144)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("dark_chocolate", 144),
                        fluidIngredient("pasteurized_milk", 250)
                ),
                List.of(
                        fluidIngredient("milk_chocolate", 288)
                ), 0.5D, 0.5D
        );

    }

    protected static void add(List<FluidStackIngredient> input, List<FluidStackIngredient> output, double...modifiers) {
        fluidsAndFluids(input, output, modifiers);
    }
}
