package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.fluid.Fluids.WATER;

public class ChemicalReactorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        ChemicalReactorRecipes.consumer = consumer;
        ID = Processors.CHEMICAL_REACTOR;

        add(
                Arrays.asList(
                        fluidIngredient(Materials.arsenic, 333),
                        fluidIngredient(Materials.boron, 72)
                ),
                Arrays.asList(
                        fluidStack(Materials.boron_arsenide, 288)
                ), 0.5D, 1.2D
        );

        add(
                Arrays.asList(
                        fluidIngredient("ammonia", 350),
                        fluidIngredient("oxygen", 650)
                ),
                Arrays.asList(
                        fluidStack("nitric_oxide", 750),
                        fluidStack(WATER, 250)
                ), 0.5D, 0.6D
        );

        add(
                Arrays.asList(
                        fluidIngredient("nitric_oxide", 500),
                        fluidIngredient("oxygen", 250)
                ),
                Arrays.asList(
                        fluidStack("nitrogen_dioxide", 750)
                ), 0.5D, 0.6D
        );

        add(
                Arrays.asList(
                        fluidIngredient("nitrogen_dioxide", 750),
                        fluidIngredient("minecraft:water", 250)
                ),
                Arrays.asList(
                        fluidStack("nitric_acid", 1000)
                ), 1.5D, 0.4D
        );

        add(
                Arrays.asList(
                        fluidIngredient("nitric_acid", 250),
                        fluidIngredient("hydrochloric_acid", 750)
                ),
                Arrays.asList(
                        fluidStack("aqua_regia_acid", 1000)
                ), 1.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("hydrogen_chloride", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                Arrays.asList(
                        fluidStack("hydrochloric_acid", 500)
                )
        );

        add(
                Arrays.asList(
                    fluidIngredient("boron", 144),
                    fluidIngredient("hydrogen", 666)
                        ),
                Arrays.asList(
                        fluidStack("diborane", 500)
                ), 0.5D
        );

        add(
                Arrays.asList(
                    fluidIngredient("diborane", 250),
                    fluidIngredient("minecraft:water", 750)
                        ),
                Arrays.asList(
                        fluidStack("boric_acid", 500),
                        fluidStack("hydrogen", 500)
                ), 0.5D
        );

        add(
                Arrays.asList(
                    fluidIngredient("boric_acid", 500),
                    fluidIngredient("ammonia", 500)
                        ),
                Arrays.asList(
                        fluidStack("boron_nitride_solution", 72),
                        fluidStack(WATER, 1000)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("nitrogen", 250),
                        fluidIngredient("hydrogen", 750)
                ),
                Arrays.asList(
                        fluidStack("ammonia", 750)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("hydrogen", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                Arrays.asList(
                        fluidStack(WATER, 500)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("deuterium", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                Arrays.asList(
                        fluidStack("heavy_water", 500)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("hydrogen", 250),
                        fluidStackIngredient("fluorine", 250)
                ),
                Arrays.asList(
                        fluidStack("hydrofluoric_acid", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("lithium", 288),
                        fluidIngredient("fluorine", 250)
                ),
                Arrays.asList(
                        fluidStack("lithium_fluoride", 288)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("beryllium", 288),
                        fluidIngredient("fluorine", 250)
                ),
                Arrays.asList(
                        fluidStack("beryllium_fluoride", 288)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("sulfur", 72),
                        fluidIngredient("liquid_oxygen", 500)
                ),
                Arrays.asList(
                        fluidStack("sulfur_dioxide", 500)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("sulfur_dioxide", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                Arrays.asList(
                        fluidStack("sulfur_trioxide", 500)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("sulfur_trioxide", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                Arrays.asList(
                        fluidStack("sulfuric_acid", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("fluorite_water", 500),
                        fluidIngredient("sulfuric_acid", 500)
                ),
                Arrays.asList(
                        fluidStack("hydrofluoric_acid", 1000),
                        fluidStack("calcium_sulfate_solution", 50)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("sodium_fluoride_solution", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                Arrays.asList(
                        fluidStack("sodium_hydroxide_solution", 72),
                        fluidStack("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("potassium_fluoride_solution", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                Arrays.asList(
                        fluidStack("potassium_hydroxide_solution", 72),
                        fluidStack("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("sodium_fluoride_solution", 144),
                        fluidIngredient("boric_acid", 2000)
                ),
                Arrays.asList(
                        fluidStack("borax_solution", 72),
                        fluidStack("hydrofluoric_acid", 1500)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("oxygen_difluoride", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                Arrays.asList(
                        fluidStack("liquid_oxygen", 250),
                        fluidStack("hydrofluoric_acid", 250)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("oxygen_difluoride", 250),
                        fluidIngredient("sulfur_dioxide", 250)
                ),
                Arrays.asList(
                        fluidStack("sulfur_trioxide", 250),
                        fluidStack("fluorine", 250)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("liquid_oxygen", 250),
                        fluidIngredient("fluorine", 500)
                ),
                Arrays.asList(
                        fluidStack("oxygen_difluoride", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("manganese_dioxide", 72),
                        fluidIngredient("carbon", 144)
                ),
                Arrays.asList(
                        fluidStack("manganese", 72),
                        fluidStack("carbon_monoxide", 750)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("sugar", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                Arrays.asList(
                        fluidStack("ethanol", 2000),
                        fluidStack("carbon_dioxide", 1000)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("carbon_dioxide", 250),
                        fluidIngredient("hydrogen", 250)
                ),
                Arrays.asList(
                        fluidStack("carbon_monoxide", 250),
                        fluidStack(WATER, 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("carbon_monoxide", 250),
                        fluidIngredient("hydrogen", 500)
                ),
                Arrays.asList(
                        fluidStack("methanol", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("methanol", 250),
                        fluidIngredient("hydrofluoric_acid", 250)
                ),
                Arrays.asList(
                        fluidStack("fluoromethane", 250),
                        fluidStack(WATER, 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("sodium_hydroxide_solution", 72)
                ),
                Arrays.asList(
                        fluidStack("ethene", 250),
                        fluidStack("sodium_fluoride_solution", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("potassium_hydroxide_solution", 72)
                ),
                Arrays.asList(
                        fluidStack("ethene", 250),
                        fluidStack("potassium_fluoride_solution", 250)
                ), 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("ethene", 250),
                        fluidIngredient("sulfuric_acid", 250)
                ),
                Arrays.asList(
                        fluidStack("ethanol", 250),
                        fluidStack("sulfur_trioxide", 250)
                ), 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("ice", 350),
                        fluidIngredient("ethanol", 150)
                ),
                Arrays.asList(
                        fluidStack("slurry_ice", 500)
                )
        );

        add(
                Arrays.asList(
                        fluidIngredient("boron_arsenide", 250),
                        fluidIngredient("minecraft:water", 100)
                ),
                Arrays.asList(
                        fluidStack("boron_arsenide_solution", 350)
                )
        );

        add(
                Arrays.asList(
                        fluidIngredient("hydrogen", 250),
                        fluidIngredient("chlorine", 150)
                ),
                Arrays.asList(
                        fluidStack("hydrogen_chloride", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("gelatin", 72),
                        fluidIngredient("minecraft:water", 250)
                ),
                Arrays.asList(
                        fluidStack("hydrated_gelatin", 250)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("hydrated_gelatin", 144),
                        fluidIngredient("sugar", 72)
                ),
                Arrays.asList(
                        fluidStack("marshmallow", 144)
                ), 1D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("chocolate_liquor", 72),
                        fluidIngredient("cocoa_butter", 72)
                ),
                Arrays.asList(
                        fluidStack("unsweetened_chocolate", 144)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("unsweetened_chocolate", 144),
                        fluidIngredient("sugar", 72)
                ),
                Arrays.asList(
                        fluidStack("dark_chocolate", 144)
                ), 0.5D, 0.5D
        );

        add(
                Arrays.asList(
                        fluidIngredient("dark_chocolate", 144),
                        fluidIngredient("pasteurized_milk", 250)
                ),
                Arrays.asList(
                        fluidStack("milk_chocolate", 288)
                ), 0.5D, 0.5D
        );

    }

    protected static void add(List<FluidStackIngredient> input, List<FluidStack> output, double...modifiers) {
        fluidsAndFluids(input, output, modifiers);
    }
}
