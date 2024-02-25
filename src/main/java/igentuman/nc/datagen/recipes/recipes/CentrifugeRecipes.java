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
                        fluidIngredient(Materials.uranium238, 144),
                        fluidIngredient(Materials.uranium235, 16)
                ), 0.9D
        );

        add(
                fluidIngredient("fissile_fuel", 500),
                List.of(
                        fluidIngredient(Materials.uranium238, 300),
                        fluidIngredient(Materials.uranium235, 80),
                        fluidIngredient(Materials.uranium233, 80),
                        fluidIngredient("hydrofluoric_acid", 16),
                        fluidIngredient("sulfuric_acid", 16)

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
                moltenFuelIngredient(List.of("depleted", "americium", "hea-242", ""), 1296),
                List.of(
                        fluidIngredient(Materials.americium243, 432), fluidIngredient(Materials.curium243, 144),
                        fluidIngredient(Materials.curium246, 288), fluidIngredient(Materials.berkelium247, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "americium", "lea-242", ""), 1296),
                List.of(
                        fluidIngredient(Materials.americium243, 432), fluidIngredient(Materials.curium243, 144),
                        fluidIngredient(Materials.curium246, 432), fluidIngredient(Materials.berkelium248, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "thorium", "tbu", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium233, 144), fluidIngredient(Materials.uranium238, 720),
                        fluidIngredient(Materials.neptunium236, 144), fluidIngredient(Materials.neptunium237, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "leu-233", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 720), fluidIngredient(Materials.plutonium241, 144),
                        fluidIngredient(Materials.plutonium242, 144), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "heu-233", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium235, 144), fluidIngredient(Materials.uranium238, 288),
                        fluidIngredient(Materials.plutonium242, 432), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "leu-235", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 576), fluidIngredient(Materials.plutonium239, 144),
                        fluidIngredient(Materials.plutonium242, 144), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "uranium", "heu-235", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 288), fluidIngredient(Materials.plutonium239, 144),
                        fluidIngredient(Materials.plutonium242, 432), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "neptunium", "len-236", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 576), fluidIngredient(Materials.neptunium237, 144),
                        fluidIngredient(Materials.plutonium241, 144), fluidIngredient(Materials.plutonium242, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "neptunium", "hen-236", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 576), fluidIngredient(Materials.plutonium238, 144),
                        fluidIngredient(Materials.plutonium241, 144), fluidIngredient(Materials.plutonium242, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "lep-239", ""), 1296),
                List.of(
                        fluidIngredient(Materials.curium246, 576), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.americium242, 144), fluidIngredient(Materials.plutonium242, 720),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "hep-239", ""), 1296),
                List.of(
                        fluidIngredient(Materials.americium243, 576), fluidIngredient(Materials.plutonium238, 144),
                        fluidIngredient(Materials.plutonium241, 144), fluidIngredient(Materials.americium242, 144),
                        fluidIngredient(Materials.caesium_137, 144), fluidIngredient(Materials.strontium_90, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "lep-241", ""), 1296),
                List.of(
                        fluidIngredient(Materials.plutonium242, 720), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.curium246, 144), fluidIngredient(Materials.berkelium247, 144),
                        fluidIngredient(Materials.promethium_147, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "plutonium", "hep-241", ""), 1296),
                List.of(
                        fluidIngredient(Materials.americium243, 576), fluidIngredient(Materials.curium243, 144),
                        fluidIngredient(Materials.americium242, 144), fluidIngredient(Materials.plutonium241, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "mixed", "mix-239", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 576), fluidIngredient(Materials.plutonium239, 144),
                        fluidIngredient(Materials.plutonium242, 144), fluidIngredient(Materials.americium243, 144),
                        fluidIngredient(Materials.strontium_90, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "mixed", "mix-241", ""), 1296),
                List.of(
                        fluidIngredient(Materials.uranium238, 576), fluidIngredient(Materials.neptunium237, 144),
                        fluidIngredient(Materials.plutonium241, 144), fluidIngredient(Materials.plutonium242, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.caesium_137, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-243", ""), 1296),
                List.of(
                        fluidIngredient(Materials.curium246, 576), fluidIngredient(Materials.curium247, 144),
                        fluidIngredient(Materials.berkelium247, 288), fluidIngredient(Materials.berkelium248, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-243", ""), 1296),
                List.of(
                        fluidIngredient(Materials.curium245, 432), fluidIngredient(Materials.curium245, 144),
                        fluidIngredient(Materials.berkelium247, 288), fluidIngredient(Materials.berkelium248, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-245", ""), 1296),
                List.of(
                        fluidIngredient(Materials.curium246, 576), fluidIngredient(Materials.curium247, 144),
                        fluidIngredient(Materials.berkelium247, 288), fluidIngredient(Materials.californium249, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.europium_155, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-245", ""), 1296),
                List.of(
                        fluidIngredient(Materials.curium246, 432), fluidIngredient(Materials.curium247, 144),
                        fluidIngredient(Materials.berkelium247, 288), fluidIngredient(Materials.californium249, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.europium_155, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "lecm-247", ""), 1296),
                List.of(
                        fluidIngredient(Materials.curium246, 720), fluidIngredient(Materials.berkelium247, 144),
                        fluidIngredient(Materials.californium249, 144), fluidIngredient(Materials.berkelium248, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.europium_155, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "curium", "hecm-247", ""), 1296),
                List.of(
                        fluidIngredient(Materials.californium251, 144), fluidIngredient(Materials.californium249, 144),
                        fluidIngredient(Materials.berkelium247, 576), fluidIngredient(Materials.berkelium248, 144),
                        fluidIngredient(Materials.molybdenum, 144), fluidIngredient(Materials.europium_155, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "berkelium", "leb-248", ""), 1296),
                List.of(
                        fluidIngredient(Materials.berkelium247, 720), fluidIngredient(Materials.berkelium248, 144),
                        fluidIngredient(Materials.californium249, 144), fluidIngredient(Materials.californium251, 144),
                        fluidIngredient(Materials.ruthenium_106, 144), fluidIngredient(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "berkelium", "heb-248", ""), 1296),
                List.of(
                        fluidIngredient(Materials.berkelium248, 144), fluidIngredient(Materials.californium249, 144),
                        fluidIngredient(Materials.californium251, 288), fluidIngredient(Materials.californium252, 3),
                        fluidIngredient(Materials.ruthenium_106, 144), fluidIngredient(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "lecf-249", ""), 1296),
                List.of(
                        fluidIngredient(Materials.californium252, 1152),
                        fluidIngredient(Materials.ruthenium_106, 144), fluidIngredient(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "hecf-249", ""), 1296),
                List.of(
                        fluidIngredient(Materials.californium252, 864), fluidIngredient(Materials.californium250, 288),
                        fluidIngredient(Materials.ruthenium_106, 144), fluidIngredient(Materials.promethium_147, 144)
                ),1.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "lecf-251", ""), 1296),
                List.of(
                        fluidIngredient(Materials.californium252, 1152),
                        fluidIngredient(Materials.ruthenium_106, 144), fluidIngredient(Materials.promethium_147, 144)
                ),0.5D
        );

        add(
                moltenFuelIngredient(List.of("depleted", "californium", "hecf-251", ""), 1296),
                List.of(
                        fluidIngredient(Materials.californium252, 1008),
                        fluidIngredient(Materials.ruthenium_106, 144), fluidIngredient(Materials.promethium_147, 144)
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
