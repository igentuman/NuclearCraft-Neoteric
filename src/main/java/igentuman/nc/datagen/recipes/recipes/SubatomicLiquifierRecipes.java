package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.content.materials.Materials.*;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_BLOCK;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;

public class SubatomicLiquifierRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        SubatomicLiquifierRecipes.consumer = consumer;
        ID = Processors.SUBATOMIC_LIQUIFIER;
        itemsAndFluids(
                List.of(),
                List.of(),
                List.of(fluidIngredient(quantite, MOLTEN_INGOT)),
                List.of(fluidIngredient(subliquid_matter, 10000)), 5.0D);
        List<String> isotopes = List.of(
                uranium238, uranium233, uranium235, uranium234,
                plutonium239, plutonium241, plutonium242,
                neptunium237, neptunium236,
                thorium232, thorium230,
                americium243, americium242, americium241,
                curium246, curium247, curium243, curium245,
                berkelium247, berkelium248,
                californium249, californium250, californium251, californium252,
                europium_155, promethium_147, ruthenium_106
        );
        for(String isotope: isotopes) {
            itemsAndFluids(
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(isotope, MOLTEN_INGOT)),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_INGOT*10)), 3.0D);
        }
        for(String type: List.of(potassium,"iron", "copper", "tin", "lead", "gold", "silver", "aluminum", "aluminium", "uranium", "thorium", "cobalt", "magnesium", "boron", "lithium")) {
            itemsAndFluids(
                    List.of(dustIngredient(type)),
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_INGOT*5)));
            itemsAndFluids(
                    List.of(ingotIngredient(type)),
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_INGOT*5)), 1.2D);
            itemsAndFluids(
                    List.of(blockIngredient(type)),
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_BLOCK*5)), 4.0D);
        }
    }
}
