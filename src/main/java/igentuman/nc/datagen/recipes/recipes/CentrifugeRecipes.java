package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class CentrifugeRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        CentrifugeRecipes.consumer = consumer;
        ID = Processors.CENTRIFUGE;

        add(
                fluidIngredient(Materials.uranium, 160),
                List.of(
                        fluidStack(Materials.uranium238, 144),
                        fluidStack(Materials.uranium235, 16)
                ), 0.9D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "americium", "hea-242", ""), 1296),
                List.of(
                        fluidStack(Materials.americium243, 432), fluidStack(Materials.curium243, 144),
                        fluidStack(Materials.curium246, 288), fluidStack(Materials.berkelium247, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "americium", "lea-242", ""), 1296),
                List.of(
                        fluidStack(Materials.americium243, 432), fluidStack(Materials.curium243, 144),
                        fluidStack(Materials.curium246, 432), fluidStack(Materials.berkelium248, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "thorium", "tbu", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium233, 144), fluidStack(Materials.uranium238, 720),
                        fluidStack(Materials.neptunium236, 144), fluidStack(Materials.neptunium237, 144),
                        fluidStack(Materials.strontium_90, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "leu-233", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 720), fluidStack(Materials.plutonium241, 144),
                        fluidStack(Materials.plutonium242, 144), fluidStack(Materials.americium243, 144),
                        fluidStack(Materials.strontium_90, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "heu-233", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium235, 144), fluidStack(Materials.uranium238, 288),
                        fluidStack(Materials.plutonium242, 432), fluidStack(Materials.americium243, 144),
                        fluidStack(Materials.strontium_90, 144), fluidStack(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "leu-235", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 576), fluidStack(Materials.plutonium239, 144),
                        fluidStack(Materials.plutonium242, 144), fluidStack(Materials.americium243, 144),
                        fluidStack(Materials.strontium_90, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "heu-235", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 288), fluidStack(Materials.plutonium239, 144),
                        fluidStack(Materials.plutonium242, 432), fluidStack(Materials.americium243, 144),
                        fluidStack(Materials.strontium_90, 144), fluidStack(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "neptunium", "len-236", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 576), fluidStack(Materials.neptunium237, 144),
                        fluidStack(Materials.plutonium241, 144), fluidStack(Materials.plutonium242, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "neptunium", "hen-236", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 288), fluidStack(Materials.neptunium237, 144),
                        fluidStack(Materials.plutonium241, 144), fluidStack(Materials.plutonium242, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "lep-239", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 576), fluidStack(Materials.plutonium238, 144),
                        fluidStack(Materials.plutonium241, 144), fluidStack(Materials.plutonium242, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "hep-239", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 288), fluidStack(Materials.plutonium238, 144),
                        fluidStack(Materials.plutonium241, 144), fluidStack(Materials.plutonium242, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "lep-241", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 576), fluidStack(Materials.neptunium237, 144),
                        fluidStack(Materials.plutonium238, 144), fluidStack(Materials.plutonium241, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "hep-241", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 288), fluidStack(Materials.neptunium237, 144),
                        fluidStack(Materials.plutonium238, 144), fluidStack(Materials.plutonium241, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "mixed", "mix-239", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 576), fluidStack(Materials.plutonium239, 144),
                        fluidStack(Materials.plutonium242, 144), fluidStack(Materials.americium243, 144),
                        fluidStack(Materials.strontium_90, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "mixed", "mix-241", ""), 1296),
                List.of(
                        fluidStack(Materials.uranium238, 576), fluidStack(Materials.neptunium237, 144),
                        fluidStack(Materials.plutonium241, 144), fluidStack(Materials.plutonium242, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-243", ""), 1296),
                List.of(
                        fluidStack(Materials.curium246, 576), fluidStack(Materials.curium247, 144),
                        fluidStack(Materials.berkelium247, 288), fluidStack(Materials.berkelium248, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-243", ""), 1296),
                List.of(
                        fluidStack(Materials.curium245, 432), fluidStack(Materials.curium245, 144),
                        fluidStack(Materials.berkelium247, 288), fluidStack(Materials.berkelium248, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-245", ""), 1296),
                List.of(
                        fluidStack(Materials.curium246, 576), fluidStack(Materials.curium247, 144),
                        fluidStack(Materials.berkelium247, 288), fluidStack(Materials.californium249, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.europium_155, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-245", ""), 1296),
                List.of(
                        fluidStack(Materials.curium246, 432), fluidStack(Materials.curium247, 144),
                        fluidStack(Materials.berkelium247, 288), fluidStack(Materials.californium249, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.europium_155, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-247", ""), 1296),
                List.of(
                        fluidStack(Materials.curium246, 720), fluidStack(Materials.berkelium247, 144),
                        fluidStack(Materials.californium249, 144), fluidStack(Materials.berkelium248, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.europium_155, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-247", ""), 1296),
                List.of(
                        fluidStack(Materials.californium251, 144), fluidStack(Materials.californium249, 144),
                        fluidStack(Materials.berkelium247, 576), fluidStack(Materials.berkelium248, 144),
                        fluidStack(Materials.molybdenum, 144), fluidStack(Materials.europium_155, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "berkelium", "leb-248", ""), 1296),
                List.of(
                        fluidStack(Materials.berkelium247, 720), fluidStack(Materials.berkelium248, 144),
                        fluidStack(Materials.californium249, 144), fluidStack(Materials.californium251, 144),
                        fluidStack(Materials.ruthenium_106, 144), fluidStack(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "berkelium", "heb-248", ""), 1296),
                List.of(
                        fluidStack(Materials.berkelium248, 144), fluidStack(Materials.californium249, 144),
                        fluidStack(Materials.californium251, 288), fluidStack(Materials.californium252, 3),
                        fluidStack(Materials.ruthenium_106, 144), fluidStack(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "lecf-249", ""), 1296),
                List.of(
                        fluidStack(Materials.californium252, 1152),
                        fluidStack(Materials.ruthenium_106, 144), fluidStack(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "hecf-249", ""), 1296),
                List.of(
                        fluidStack(Materials.californium252, 864), fluidStack(Materials.californium250, 288),
                        fluidStack(Materials.ruthenium_106, 144), fluidStack(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "lecf-251", ""), 1296),
                List.of(
                        fluidStack(Materials.californium252, 1152),
                        fluidStack(Materials.ruthenium_106, 144), fluidStack(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "hecf-251", ""), 1296),
                List.of(
                        fluidStack(Materials.californium252, 1008),
                        fluidStack(Materials.ruthenium_106, 144), fluidStack(Materials.promethium_147, 144)
                ),1.5D
        );

    }

    protected static void add(FluidStackIngredient input, List<FluidStack> output, double...modifiers) {
        fluidsAndFluids(List.of(input), output, modifiers);
    }
}
