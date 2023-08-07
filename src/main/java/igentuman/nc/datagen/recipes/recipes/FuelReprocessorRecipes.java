package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class FuelReprocessorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        FuelReprocessorRecipes.consumer = consumer;
        ID = Processors.FUEL_REPROCESSOR;
        add(
                fuelIngredient(List.of("depleted", "americium", "hea-242", ""), 9),
                List.of(
                        isotopeStack(Materials.americium243, 3), isotopeStack(Materials.curium243),
                        isotopeStack(Materials.curium246, 2), isotopeStack(Materials.berkelium247),
                        dustStack(Materials.molybdenum), dustStack(Materials.promethium_147)
                        ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "americium", "lea-242", ""), 9),
                List.of(
                        isotopeStack(Materials.americium243, 3), isotopeStack(Materials.curium243),
                        isotopeStack(Materials.curium246, 3), isotopeStack(Materials.berkelium248),
                        dustStack(Materials.molybdenum), dustStack(Materials.promethium_147)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "thorium", "tbu", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium233), isotopeStack(Materials.uranium238, 5),
                        isotopeStack(Materials.neptunium236), isotopeStack(Materials.neptunium237),
                        dustStack(Materials.strontium_90), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "uranium", "leu-233", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 5), isotopeStack(Materials.plutonium241),
                        isotopeStack(Materials.plutonium242), isotopeStack(Materials.americium243),
                        dustStack(Materials.strontium_90), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "uranium", "heu-233", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium235), isotopeStack(Materials.uranium238, 2),
                        isotopeStack(Materials.plutonium242, 3), isotopeStack(Materials.americium243),
                        dustStack(Materials.strontium_90), dustStack(Materials.caesium_137)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "uranium", "leu-235", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 4), isotopeStack(Materials.plutonium239),
                        isotopeStack(Materials.plutonium242), isotopeStack(Materials.americium243),
                        dustStack(Materials.strontium_90), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "uranium", "heu-235", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 2), isotopeStack(Materials.plutonium239),
                        isotopeStack(Materials.plutonium242, 3), isotopeStack(Materials.americium243),
                        dustStack(Materials.strontium_90), dustStack(Materials.caesium_137)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "neptunium", "len-236", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 4), isotopeStack(Materials.neptunium237),
                        isotopeStack(Materials.plutonium241), isotopeStack(Materials.plutonium242),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "neptunium", "hen-236", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 2), isotopeStack(Materials.neptunium237),
                        isotopeStack(Materials.plutonium241), isotopeStack(Materials.plutonium242),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "plutonium", "lep-239", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 4), isotopeStack(Materials.plutonium238),
                        isotopeStack(Materials.plutonium241), isotopeStack(Materials.plutonium242),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "plutonium", "hep-239", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 2), isotopeStack(Materials.plutonium238),
                        isotopeStack(Materials.plutonium241), isotopeStack(Materials.plutonium242),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "plutonium", "lep-241", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 4), isotopeStack(Materials.neptunium237),
                        isotopeStack(Materials.plutonium238), isotopeStack(Materials.plutonium241),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "plutonium", "hep-241", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 2), isotopeStack(Materials.neptunium237),
                        isotopeStack(Materials.plutonium238), isotopeStack(Materials.plutonium241),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "mixed", "mix-239", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 4), isotopeStack(Materials.plutonium239),
                        isotopeStack(Materials.plutonium242), isotopeStack(Materials.americium243),
                        dustStack(Materials.strontium_90), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "mixed", "mix-241", ""), 9),
                List.of(
                        isotopeStack(Materials.uranium238, 4), isotopeStack(Materials.neptunium237),
                        isotopeStack(Materials.plutonium241), isotopeStack(Materials.plutonium242),
                        dustStack(Materials.molybdenum), dustStack(Materials.caesium_137)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "curium", "lecm-243", ""), 9),
                List.of(
                        isotopeStack(Materials.curium246, 4), isotopeStack(Materials.curium247),
                        isotopeStack(Materials.berkelium247, 2), isotopeStack(Materials.berkelium248),
                        dustStack(Materials.molybdenum), dustStack(Materials.promethium_147)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "curium", "hecm-243", ""), 9),
                List.of(
                        isotopeStack(Materials.curium245, 3), isotopeStack(Materials.curium245),
                        isotopeStack(Materials.berkelium247, 2), isotopeStack(Materials.berkelium248),
                        dustStack(Materials.molybdenum), dustStack(Materials.promethium_147)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "curium", "lecm-245", ""), 9),
                List.of(
                        isotopeStack(Materials.curium246, 4), isotopeStack(Materials.curium247),
                        isotopeStack(Materials.berkelium247, 2), isotopeStack(Materials.californium249),
                        dustStack(Materials.molybdenum), dustStack(Materials.europium_155)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "curium", "hecm-245", ""), 9),
                List.of(
                        isotopeStack(Materials.curium246, 3), isotopeStack(Materials.curium247),
                        isotopeStack(Materials.berkelium247, 2), isotopeStack(Materials.californium249),
                        dustStack(Materials.molybdenum), dustStack(Materials.europium_155)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "curium", "lecm-247", ""), 9),
                List.of(
                        isotopeStack(Materials.curium246, 5), isotopeStack(Materials.berkelium247),
                        isotopeStack(Materials.californium249), isotopeStack(Materials.berkelium248),
                        dustStack(Materials.molybdenum), dustStack(Materials.europium_155)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "curium", "hecm-247", ""), 9),
                List.of(
                        isotopeStack(Materials.californium251), isotopeStack(Materials.californium249),
                        isotopeStack(Materials.berkelium247, 4), isotopeStack(Materials.berkelium248),
                        dustStack(Materials.molybdenum), dustStack(Materials.europium_155)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "berkelium", "leb-248", ""), 9),
                List.of(
                        isotopeStack(Materials.berkelium247, 5), isotopeStack(Materials.berkelium248),
                        isotopeStack(Materials.californium249), isotopeStack(Materials.californium251),
                        dustStack(Materials.ruthenium_106), dustStack(Materials.promethium_147)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "berkelium", "heb-248", ""), 9),
                List.of(
                        isotopeStack(Materials.berkelium248), isotopeStack(Materials.californium249),
                        isotopeStack(Materials.californium251, 2), isotopeStack(Materials.californium252, 3),
                        dustStack(Materials.ruthenium_106), dustStack(Materials.promethium_147)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "californium", "lecf-249", ""), 9),
                List.of(
                        isotopeStack(Materials.californium252, 8),
                        dustStack(Materials.ruthenium_106), dustStack(Materials.promethium_147)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "californium", "hecf-249", ""), 9),
                List.of(
                        isotopeStack(Materials.californium252, 6), isotopeStack(Materials.californium250, 2),
                        dustStack(Materials.ruthenium_106), dustStack(Materials.promethium_147)
                ),1.5D
        );

        add(
                fuelIngredient(List.of("depleted", "californium", "lecf-251", ""), 9),
                List.of(
                        isotopeStack(Materials.californium252, 8),
                        dustStack(Materials.ruthenium_106), dustStack(Materials.promethium_147)
                ),0.5D
        );

        add(
                fuelIngredient(List.of("depleted", "californium", "hecf-251", ""), 9),
                List.of(
                        isotopeStack(Materials.californium252, 7),
                        dustStack(Materials.ruthenium_106), dustStack(Materials.promethium_147)
                ),1.5D
        );

    }

    private static void add(NcIngredient input, List<NcIngredient> output, double...modifiers) {
        itemsToItems(List.of(input), output, modifiers);
    }

}
