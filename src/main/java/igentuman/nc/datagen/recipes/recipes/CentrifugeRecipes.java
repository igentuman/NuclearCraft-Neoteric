package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.datagen.recipes.NCRecipes.*;

public class CentrifugeRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        CentrifugeRecipes.consumer = consumer;
        ID = Processors.CENTRIFUGE;

        add(
                fluidIngredient("carbon_dioxide", 1000),
                List.of(
                        fluidIngredient("carbon_dioxide", 125),
                        fluidIngredient("carbon_monoxide", 125),
                        fluidIngredient("carbon", 375),
                        fluidIngredient("oxygen", 375)
                )
        );

        add(
                fluidIngredient("irradiated_boron", 1000),
                List.of(
                        fluidIngredient(Materials.boron10, 500),
                        fluidIngredient(Materials.boron11, 500)
                ), 1.5D
        );

        add(
                fluidIngredient("irradiated_lithium", 1000),
                List.of(
                        fluidIngredient(Materials.lithium6, 250),
                        fluidIngredient(Materials.lithium7, 250),
                        fluidIngredient("tritium", 500)
                )
        );

        add(
                fluidIngredient("technical_water", 1000),
                List.of(
                        fluidIngredient("deuterium", 750),
                        fluidIngredient("oxygen", 250)
                ), 1.9D
        );

        add(
                fluidIngredient(Materials.uranium, 160),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT),
                        fluidIngredient(Materials.uranium235, MOLTEN_NUGGET)
                ), 0.9D
        );

        add(
                fluidIngredient("fissile_fuel", 500),
                List.of(
                        fluidIngredient(Materials.uranium238, 300),
                        fluidIngredient(Materials.uranium235, 80),
                        fluidIngredient(Materials.uranium233, 80),
                        fluidIngredient("hydrofluoric_acid", MOLTEN_NUGGET),
                        fluidIngredient("sulfuric_acid", MOLTEN_NUGGET)

                ), 1.9D
        );

        add(
                fluidIngredient("nuclear_waste", 50),
                List.of(
                        fluidIngredient(Materials.polonium, 5),
                        fluidIngredient(Materials.plutonium238, 5),
                        fluidIngredient(Materials.plutonium242, 5),
                        fluidIngredient(Materials.plutonium241, 5),
                        fluidIngredient("spent_nuclear_waste", 1)

                ), 15D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "americium", "hea-242", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.americium243, MOLTEN_INGOT*3), fluidIngredient(Materials.curium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*2), fluidIngredient(Materials.berkelium247, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "americium", "lea-242", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.americium243, MOLTEN_INGOT*3), fluidIngredient(Materials.curium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*3), fluidIngredient(Materials.berkelium248, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "thorium", "tbu", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium233, MOLTEN_INGOT), fluidIngredient(Materials.uranium238, MOLTEN_INGOT*5),
                        fluidIngredient(Materials.neptunium236, MOLTEN_INGOT), fluidIngredient(Materials.neptunium237, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "leu-233", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*5), fluidIngredient(Materials.plutonium241, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium242, MOLTEN_INGOT), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "heu-233", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium235, MOLTEN_INGOT), fluidIngredient(Materials.uranium238, MOLTEN_INGOT*2),
                        fluidIngredient(Materials.plutonium242, MOLTEN_INGOT*3), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "leu-235", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*4), fluidIngredient(Materials.plutonium239, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium242, MOLTEN_INGOT), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "heu-235", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*2), fluidIngredient(Materials.plutonium239, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium242, MOLTEN_INGOT*3), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "neptunium", "len-236", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*4), fluidIngredient(Materials.neptunium237, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium241, MOLTEN_INGOT), fluidIngredient(Materials.plutonium242, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "neptunium", "hen-236", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*4), fluidIngredient(Materials.plutonium238, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium241, MOLTEN_INGOT), fluidIngredient(Materials.plutonium242, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "lep-239", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*4), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.americium242, MOLTEN_INGOT), fluidIngredient(Materials.plutonium242, MOLTEN_INGOT*5),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "hep-239", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.americium243, MOLTEN_INGOT*4), fluidIngredient(Materials.plutonium238, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium241, MOLTEN_INGOT), fluidIngredient(Materials.americium242, MOLTEN_INGOT),
                        fluidIngredient(Materials.caesium_137, MOLTEN_INGOT), fluidIngredient(Materials.strontium_90, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "lep-241", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.plutonium242, MOLTEN_INGOT*5), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT), fluidIngredient(Materials.berkelium247, MOLTEN_INGOT),
                        fluidIngredient(Materials.promethium_147, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "hep-241", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.americium243, MOLTEN_INGOT*4), fluidIngredient(Materials.curium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.americium242, MOLTEN_INGOT), fluidIngredient(Materials.plutonium241, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "mixed", "mix-239", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*4), fluidIngredient(Materials.plutonium239, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium242, MOLTEN_INGOT), fluidIngredient(Materials.americium243, MOLTEN_INGOT),
                        fluidIngredient(Materials.strontium_90, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "mixed", "mix-241", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.uranium238, MOLTEN_INGOT*4), fluidIngredient(Materials.neptunium237, MOLTEN_INGOT),
                        fluidIngredient(Materials.plutonium241, MOLTEN_INGOT), fluidIngredient(Materials.plutonium242, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.caesium_137, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-243", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*4), fluidIngredient(Materials.curium247, MOLTEN_INGOT),
                        fluidIngredient(Materials.berkelium247, MOLTEN_INGOT*2), fluidIngredient(Materials.berkelium248, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-243", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.curium245, MOLTEN_INGOT*3), fluidIngredient(Materials.curium245, MOLTEN_INGOT),
                        fluidIngredient(Materials.berkelium247, MOLTEN_INGOT*2), fluidIngredient(Materials.berkelium248, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-245", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*4), fluidIngredient(Materials.curium247, MOLTEN_INGOT),
                        fluidIngredient(Materials.berkelium247, MOLTEN_INGOT*2), fluidIngredient(Materials.californium249, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.europium_155, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-245", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*3), fluidIngredient(Materials.curium247, MOLTEN_INGOT),
                        fluidIngredient(Materials.berkelium247, MOLTEN_INGOT*2), fluidIngredient(Materials.californium249, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.europium_155, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-247", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.curium246, MOLTEN_INGOT*5), fluidIngredient(Materials.berkelium247, MOLTEN_INGOT),
                        fluidIngredient(Materials.californium249, MOLTEN_INGOT), fluidIngredient(Materials.berkelium248, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.europium_155, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-247", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.californium251, MOLTEN_INGOT), fluidIngredient(Materials.californium249, MOLTEN_INGOT),
                        fluidIngredient(Materials.berkelium247, MOLTEN_INGOT*4), fluidIngredient(Materials.berkelium248, MOLTEN_INGOT),
                        fluidIngredient(Materials.molybdenum, MOLTEN_INGOT), fluidIngredient(Materials.europium_155, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "berkelium", "leb-248", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.berkelium247, MOLTEN_INGOT*5), fluidIngredient(Materials.berkelium248, MOLTEN_INGOT),
                        fluidIngredient(Materials.californium249, MOLTEN_INGOT), fluidIngredient(Materials.californium251, MOLTEN_INGOT),
                        fluidIngredient(Materials.ruthenium_106, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "berkelium", "heb-248", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.berkelium248, MOLTEN_INGOT), fluidIngredient(Materials.californium249, MOLTEN_INGOT),
                        fluidIngredient(Materials.californium251, MOLTEN_INGOT*2), fluidIngredient(Materials.californium252, 3),
                        fluidIngredient(Materials.ruthenium_106, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "lecf-249", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.californium252, MOLTEN_INGOT*8),
                        fluidIngredient(Materials.ruthenium_106, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "hecf-249", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.californium252, MOLTEN_INGOT*6), fluidIngredient(Materials.californium250, MOLTEN_INGOT*2),
                        fluidIngredient(Materials.ruthenium_106, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "lecf-251", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.californium252, MOLTEN_INGOT*8),
                        fluidIngredient(Materials.ruthenium_106, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "hecf-251", ""), MOLTEN_BLOCK),
                List.of(
                        fluidIngredient(Materials.californium252, MOLTEN_INGOT*7),
                        fluidIngredient(Materials.ruthenium_106, MOLTEN_INGOT), fluidIngredient(Materials.promethium_147, MOLTEN_INGOT)
                ),1.5D
        );

        for(String material: Materials.slurries()) {
            add(
                    fluidIngredient(material+"_slurry", 1000),
                    List.of(
                            fluidIngredient(material+"_clean_slurry", 800),
                            fluidIngredient("hydrochloric_acid", 50),
                            fluidIngredient("nitric_acid", 50),
                            fluidIngredient("calcium_sulfate_solution", 10)
                    )
            );
        }
    }

    protected static void add(FluidStackIngredient input, List<FluidStackIngredient> output, double...modifiers) {
        fluidsAndFluids(List.of(input), output, modifiers);
    }
}
