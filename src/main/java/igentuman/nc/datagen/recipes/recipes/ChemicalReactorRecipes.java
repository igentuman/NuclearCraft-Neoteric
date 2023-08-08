package igentuman.nc.datagen.recipes.recipes;

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
                    fluidIngredient("boron", 144),
                    fluidIngredient("hydrogen", 666)
                        ),
                List.of(
                        fluidStack("diborane", 500)
                ), 0.5D
        );

        add(
                List.of(
                    fluidIngredient("diborane", 250),
                    fluidIngredient("minecraft:water", 750)
                        ),
                List.of(
                        fluidStack("boric_acid", 500),
                        fluidStack("hydrogen", 500)
                ), 0.5D
        );

        add(
                List.of(
                    fluidIngredient("boric_acid", 500),
                    fluidIngredient("ammonia", 500)
                        ),
                List.of(
                        fluidStack("boron_nitride_solution", 72),
                        fluidStack(WATER, 1000)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("nitrogen", 250),
                        fluidIngredient("hydrogen", 750)
                ),
                List.of(
                        fluidStack("ammonia", 750)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("hydrogen", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                List.of(
                        fluidStack(WATER, 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("deuterium", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                List.of(
                        fluidStack("heavy_water", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("hydrogen", 250),
                        fluidStackIngredient("fluorine", 250)
                ),
                List.of(
                        fluidStack("hydrofluoric_acid", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("lithium", 288),
                        fluidIngredient("fluorine", 250)
                ),
                List.of(
                        fluidStack("lithium_fluoride", 288)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("beryllium", 288),
                        fluidIngredient("fluorine", 250)
                ),
                List.of(
                        fluidStack("beryllium_fluoride", 288)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur", 72),
                        fluidIngredient("liquid_oxygen", 500)
                ),
                List.of(
                        fluidStack("sulfur_dioxide", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur_dioxide", 500),
                        fluidIngredient("liquid_oxygen", 250)
                ),
                List.of(
                        fluidStack("sulfur_trioxide", 500)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sulfur_trioxide", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidStack("sulfuric_acid", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluorite_water", 500),
                        fluidIngredient("sulfuric_acid", 500)
                ),
                List.of(
                        fluidStack("hydrofluoric_acid", 1000),
                        fluidStack("calcium_sulfate_solution", 50)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sodium_fluoride_solution", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidStack("sodium_hydroxide_solution", 72),
                        fluidStack("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("potassium_fluoride_solution", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidStack("potassium_hydroxide_solution", 72),
                        fluidStack("hydrofluoric_acid", 500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sodium_fluoride_solution", 144),
                        fluidIngredient("boric_acid", 2000)
                ),
                List.of(
                        fluidStack("borax_solution", 72),
                        fluidStack("hydrofluoric_acid", 1500)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("oxygen_difluoride", 250),
                        fluidIngredient("minecraft:water", 250)
                ),
                List.of(
                        fluidStack("liquid_oxygen", 250),
                        fluidStack("hydrofluoric_acid", 250)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("oxygen_difluoride", 250),
                        fluidIngredient("sulfur_dioxide", 250)
                ),
                List.of(
                        fluidStack("sulfur_trioxide", 250),
                        fluidStack("fluorine", 250)
                ), 1D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("liquid_oxygen", 250),
                        fluidIngredient("fluorine", 500)
                ),
                List.of(
                        fluidStack("oxygen_difluoride", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("manganese_dioxide", 72),
                        fluidIngredient("carbon", 144)
                ),
                List.of(
                        fluidStack("manganese", 72),
                        fluidStack("carbon_monoxide", 750)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("sugar", 72),
                        fluidIngredient("minecraft:water", 500)
                ),
                List.of(
                        fluidStack("ethanol", 2000),
                        fluidStack("carbon_dioxide", 1000)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("carbon_dioxide", 250),
                        fluidIngredient("hydrogen", 250)
                ),
                List.of(
                        fluidStack("carbon_monoxide", 250),
                        fluidStack(WATER, 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("carbon_monoxide", 250),
                        fluidIngredient("hydrogen", 500)
                ),
                List.of(
                        fluidStack("methanol", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("methanol", 250),
                        fluidIngredient("hydrofluoric_acid", 250)
                ),
                List.of(
                        fluidStack("fluoromethane", 250),
                        fluidStack(WATER, 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("sodium_hydroxide_solution", 72)
                ),
                List.of(
                        fluidStack("ethene", 250),
                        fluidStack("sodium_fluoride_solution", 250)
                ), 0.5D, 0.5D
        );

        add(
                List.of(
                        fluidIngredient("fluoromethane", 250),
                        fluidIngredient("potassium_hydroxide_solution", 72)
                ),
                List.of(
                        fluidStack("ethene", 250),
                        fluidStack("potassium_fluoride_solution", 250)
                ), 0.5D
        );

        add(
                List.of(
                        fluidIngredient("ethene", 250),
                        fluidIngredient("sulfuric_acid", 250)
                ),
                List.of(
                        fluidStack("ethanol", 250),
                        fluidStack("sulfur_trioxide", 250)
                ), 0.5D
        );

        add(
                List.of(
                        fluidIngredient("boron", 250),
                        fluidIngredient("arsenic", 250)
                ),
                List.of(
                        fluidStack("boron_arsenide_solution", 288)
                )
        );

        add(
                List.of(
                        fluidIngredient("hydrogen", 250),
                        fluidIngredient("chlorine", 150)
                ),
                List.of(
                        fluidStack("hydrogen_chloride", 250)
                ), 0.5D, 0.5D
        );
    }

    protected static void add(List<FluidStackIngredient> input, List<FluidStack> output, double...modifiers) {
        fluidsAndFluids(input, output, modifiers);
    }
}
