package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class FusionReactorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        FusionReactorRecipes.consumer = consumer;
        ID = "fusion_core";

        add(
                List.of(
                        fluidIngredient("deuterium", 1000),
                        fluidIngredient("tritium", 1000)
                ),
                List.of(
                        fluidStack("helium", 250),
                        fluidStack("helium", 250),
                        fluidStack("helium", 250),
                        fluidStack("helium", 250)
                ), 200D, 172000D, 1.5D, 816D
        );

        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("hydrogen", 1000)
                ),
                List.of(
                        fluidStack("deuterium", 250),
                        fluidStack("deuterium", 250),
                        fluidStack("deuterium", 250),
                        fluidStack("deuterium", 250)
                ), 100D, 44200D, 1.5D, 4430D
        );
        
        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("deuterium", 1000)
                ),
                List.of(
                        fluidStack("helium_3", 250),
                        fluidStack("helium_3", 250),
                        fluidStack("helium_3", 250),
                        fluidStack("helium_3", 250)
                ), 150D, 112000D, 1.5D, 1245D
        );
        
        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("tritium", 1000)
                ),
                List.of(
                        fluidStack("helium_3", 250),
                        fluidStack("helium_3", 250),
                        fluidStack("helium_3", 250),
                        fluidStack("helium_3", 250)
                ), 200D, 3000D, 1.5D, 6050D
        );
        
        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("helium_3", 1000)
                ),
                List.of(
                        fluidStack("helium", 250),
                        fluidStack("helium", 250),
                        fluidStack("helium", 250),
                        fluidStack("helium", 250)
                ), 200D, 303000D, 1.5D, 3339D
        );
        
        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("lithium/6", 144)
                ),
                List.of(
                        fluidStack("tritium", 500),
                        fluidStack("tritium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 350D, 35100D, 1.5D,7278D
        );
        
        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("lithium/7", 144)
                ),
                List.of(
                        fluidStack("helium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 400D, 133000D, 1.5D, 5071D
        );
        
        add(
                List.of(
                        fluidIngredient("hydrogen", 1000),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*3/4),
                        fluidStack("helium", 1000*3/4),
                        fluidStack("helium", 1000*3/4),
                        fluidStack("helium", 1000*3/4)
                ), 600D, 44400D, 1.5D, 16370D
        );

        add(
                List.of(
                        fluidIngredient("deuterium", 1000),
                        fluidIngredient("deuterium", 1000)
                ),
                List.of(
                        fluidStack("hydrogen", 750),
                        fluidStack("tritium", 750),
                        fluidStack("helium", 750)
                ), 200D, 50700D, 1.5D, 1156D
        );

        add(
                List.of(
                        fluidIngredient("deuterium", 1000),
                        fluidIngredient("helium_3", 1000)
                ),
                List.of(
                        fluidStack("hydrogen", 500),
                        fluidStack("hydrogen", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 200D, 50700D, 1.5D, 1156D
        );
        
        add(
                List.of(
                        fluidIngredient("deuterium", 1000),
                        fluidIngredient("lithium/6", 144)
                ),
                List.of(
                        fluidStack("helium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 250D, 225000D, 1.5D, 2632D
        );
        
        add(
                List.of(
                        fluidIngredient("deuterium", 1000),
                        fluidIngredient("lithium/7", 144)
                ),
                List.of(
                        fluidStack("helium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 450D, 859000D, 1.5D, 5034D
        );
        
        add(
                List.of(
                        fluidIngredient("deuterium", 1000),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*6/4),
                        fluidStack("helium", 1000*6/4)
                ), 650D, 26100D, 1.5D, 16883D
        );

        add(
                List.of(
                        fluidIngredient("tritium", 1000),
                        fluidIngredient("tritium", 1000)
                ),
                List.of(
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 300D, 90100D, 1.5D, 897D
        );
        
        add(
                List.of(
                        fluidIngredient("tritium", 1000),
                        fluidIngredient("helium_3", 1000)
                ),
                List.of(
                        fluidStack("helium", 250),
                        fluidStack("helium", 250),
                        fluidStack("helium", 250),
                        fluidStack("helium", 250)
                ), 300D, 109100D, 1.5D, 2604D
        );

        add(
                List.of(
                        fluidIngredient("tritium", 1000),
                        fluidIngredient("lithium/6", 144)
                ),
                List.of(
                        fluidStack("helium", 1000),
                        fluidStack("helium", 1000)
                ), 450D, 91500D, 1.5D, 4971D
        );

        add(
                List.of(
                        fluidIngredient("tritium", 1000),
                        fluidIngredient("lithium/7", 144)
                ),
                List.of(
                        fluidStack("helium", 1000),
                        fluidStack("helium", 1000)
                ), 500D, 43500D, 1.5D, 5511D
        );

        add(
                List.of(
                        fluidIngredient("tritium", 1000),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*6/4),
                        fluidStack("helium", 1000*6/4)
                ), 700D, 700D, 1.5D, 33215D
        );

        add(
                List.of(fluidIngredient("helium_3", 1000),
                        fluidIngredient("helium_3", 1000)
                ),
                List.of(
                        fluidStack("hydrogen", 1000),
                        fluidStack("hydrogen", 1000),
                        fluidStack("helium", 500),
                        fluidStack("helium", 500)
                ), 300D, 131000D, 1.5D, 6605D
        );

        add(
                List.of(fluidIngredient("helium_3", 1000),
                        fluidIngredient("lithium/6", 144)
                ),
                List.of(
                        fluidStack("hydrogen", 500),
                        fluidStack("hydrogen", 500),
                        fluidStack("helium", 1000),
                        fluidStack("helium", 1000)
                ), 450D, 115000D, 1.5D, 9506D
        );

        add(
                List.of(fluidIngredient("helium_3", 1000),
                        fluidIngredient("lithium/7", 144)
                ),
                List.of(
                        fluidStack("deuterium", 500),
                        fluidStack("deuterium", 500),
                        fluidStack("helium", 1000),
                        fluidStack("helium", 1000)
                ), 500D, 72700D, 1.5D, 9673D
        );

        add(
                List.of(fluidIngredient("helium_3", 1000),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("deuterium", 500),
                        fluidStack("deuterium", 500),
                        fluidStack("helium", 1000*6/4),
                        fluidStack("helium", 1000*6/4)
                ), 700D, 14000D, 1.5D, 29574D
        );

        add(
                List.of(fluidIngredient("lithium/6", 144),
                        fluidIngredient("lithium/6", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*3/4),
                        fluidStack("helium", 1000*3/4),
                        fluidStack("helium", 1000*3/4),
                        fluidStack("helium", 1000*3/4)
                ), 600D, 106000D, 1.5D, 13732D
        );

        add(
                List.of(fluidIngredient("lithium/6", 144),
                        fluidIngredient("lithium/7", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*6/4),
                        fluidStack("helium", 1000*6/4)
                ), 650D, 55200D, 1.5D, 14536D
        );

        add(
                List.of(fluidIngredient("lithium/6", 144),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*2),
                        fluidStack("helium", 1000*2)
                ), 850D, 15700D, 1.5D, 37048D
        );

        add(
                List.of(fluidIngredient("lithium/7", 144),
                        fluidIngredient("lithium/7", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*6/4),
                        fluidStack("helium", 1000*6/4)
                ), 700D, 22900D, 1.5D, 16611D
        );

        add(
                List.of(fluidIngredient("lithium/7", 144),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*2),
                        fluidStack("helium", 1000*2)
                ), 900D, 45D, 1.5D, 202000D
        );

        add(
                List.of(fluidIngredient("boron/11", 144),
                        fluidIngredient("boron/11", 144)
                ),
                List.of(
                        fluidStack("helium", 1000*10/4),
                        fluidStack("helium", 1000*10/4)
                ), 20*55D, 5D, 1.5D, 358000D
        );

    }

    protected static void add(List<FluidStackIngredient> input, List<FluidStack> output, double...modifiers) {
        double timeModifier = modifiers.length>0 ? modifiers[0] : 1.0;
        double powerModifier = modifiers.length>1 ? modifiers[1] : 1.0;
        double radiation = modifiers.length>2 ? modifiers[2] : 1.0;
        double temperature = modifiers.length>3 ? modifiers[3] : 1.0;
        NcRecipeBuilder.get(ID)
                .fluids(input, output)
                .modifiers(timeModifier, radiation, powerModifier)
                .temperature(temperature)
                .build(consumer);
    }
}
