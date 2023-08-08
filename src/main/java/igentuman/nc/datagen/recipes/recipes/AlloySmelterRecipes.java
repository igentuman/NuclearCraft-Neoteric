package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import java.util.function.Consumer;

public class AlloySmelterRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AlloySmelterRecipes.consumer = consumer;
        ID = Processors.ALLOY_SMELTER;

        doubleToItem(ID, dustIngredient(Materials.coal, 4), dustIngredient(Materials.iron),
                ingotStack(Materials.steel));
        doubleToItem(ID, dustIngredient(Materials.copper, 3), dustIngredient(Materials.tin),
                ingotStack(Materials.bronze, 4));
        doubleToItem(ID, dustIngredient(Materials.boron), dustIngredient(Materials.steel),
                ingotStack(Materials.ferroboron, 2));
        doubleToItem(ID, dustIngredient(Materials.graphite), dustIngredient(Materials.diamond),
                ingotStack(Materials.hard_carbon, 2), 1d, 2d);
        doubleToItem(ID, dustIngredient(Materials.ferroboron), dustIngredient(Materials.lithium),
                ingotStack(Materials.tough_alloy, 2), 1.5d, 1.5d);
        doubleToItem(ID, dustIngredient(Materials.magnesium), dustIngredient(Materials.boron, 2),
                ingotStack(Materials.magnesium_diboride, 3));
        doubleToItem(ID, dustIngredient(Materials.lithium), dustIngredient(Materials.manganese_dioxide),
                ingotStack(Materials.lithium_manganese_dioxide, 2), 1.5d);
        doubleToItem(ID, dustIngredient(Materials.copper, 3), dustIngredient(Materials.silver),
                ingotStack(Materials.shibuichi, 4), 1.5d);
        doubleToItem(ID, dustIngredient(Materials.tin, 3), dustIngredient(Materials.silver),
                ingotStack(Materials.tin_silver, 4));
        doubleToItem(ID, dustIngredient(Materials.lead, 3), dustIngredient(Materials.platinum),
                ingotStack(Materials.lead_platinum, 4), 1.5d);
        doubleToItem(ID, dustIngredient(Materials.tough_alloy), dustIngredient(Materials.hard_carbon),
                ingotStack(Materials.extreme, 2));
        doubleToItem(ID, dustIngredient(Materials.boron_arsenide), dustIngredient(Materials.extreme),
                ingotStack(Materials.thermoconducting, 2), 1.5d, 1.5d);
        doubleToItem(ID, gemIngredient(Materials.silicon), dustIngredient(Materials.graphite),
                ingotStack(Materials.silicon_carbide, 2), 2d, 2d);
        doubleToItem(ID, dustIngredient(Materials.zirconium, 7), dustIngredient(Materials.tin),
                ingotStack(Materials.zircaloy, 8));
        doubleToItem(ID, dustIngredient(Materials.iron, 15), dustIngredient(Materials.carbon_manganese),
                ingotStack(Materials.hsla_steel, 16),8D, 2D);
        doubleToItem(ID, dustIngredient(Materials.molybdenum, 15), dustIngredient(Materials.zirconium),
                ingotStack(Materials.zirconium_molybdenum, 16),8D, 2D);
    }
}
