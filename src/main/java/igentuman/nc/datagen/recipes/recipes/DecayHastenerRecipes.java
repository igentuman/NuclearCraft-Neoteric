package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class DecayHastenerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        DecayHastenerRecipes.consumer = consumer;
        ID = Processors.DECAY_HASTENER;

        itemToItem(dustIngredient(Materials.strontium_90), dustIngredient(Materials.strontium), 2);
        itemToItem(dustIngredient(Materials.thorium, 4), isotopeStack(Materials.thorium232));
        itemToItem(dustIngredient(Materials.radium), dustIngredient(Materials.lead));
        itemToItem(dustIngredient(Materials.polonium), dustIngredient(Materials.lead));
        itemToItem(dustIngredient(Materials.protactinium_233), isotopeStack(Materials.uranium233));
        itemToItem(dustIngredient(Materials.bismuth), dustIngredient(Materials.thallium));
        itemToItem(dustIngredient(Materials.tbp), isotopeStack(Materials.thorium230));
        itemToItem(dustIngredient(Materials.ruthenium_106), dustIngredient(Materials.palladium));
        itemToItem(dustIngredient(Materials.caesium_137), dustIngredient(Materials.barium));
        itemToItem(dustIngredient(Materials.promethium_147), dustIngredient(Materials.neodymium));
        itemToItem(dustIngredient(Materials.europium_155), dustIngredient(Materials.gadolinium));
        itemToItem(isotopeIngredient(Materials.uranium233), dustIngredient(Materials.bismuth));
        itemToItem(isotopeIngredient(Materials.uranium235), dustIngredient(Materials.lead));
        itemToItem(isotopeIngredient(Materials.uranium238), dustIngredient(Materials.radium));
        itemToItem(isotopeIngredient(Materials.neptunium236), dustIngredient(Materials.thorium));
        itemToItem(isotopeIngredient(Materials.neptunium237), isotopeStack(Materials.uranium233));
        itemToItem(isotopeIngredient(Materials.plutonium238), dustIngredient(Materials.lead));
        itemToItem(isotopeIngredient(Materials.plutonium239), isotopeStack(Materials.uranium233));
        itemToItem(isotopeIngredient(Materials.plutonium241), isotopeStack(Materials.neptunium237));
        itemToItem(isotopeIngredient(Materials.plutonium242), isotopeStack(Materials.uranium238));
        itemToItem(isotopeIngredient(Materials.americium241), isotopeStack(Materials.neptunium237));
        itemToItem(isotopeIngredient(Materials.americium242), dustIngredient(Materials.lead));
        itemToItem(isotopeIngredient(Materials.americium243), isotopeStack(Materials.plutonium238));
        itemToItem(isotopeIngredient(Materials.curium243), isotopeStack(Materials.plutonium239));
        itemToItem(isotopeIngredient(Materials.curium245), isotopeStack(Materials.plutonium241));
        itemToItem(isotopeIngredient(Materials.curium246), isotopeStack(Materials.plutonium242));
        itemToItem(isotopeIngredient(Materials.curium247), isotopeStack(Materials.americium243));
        itemToItem(isotopeIngredient(Materials.berkelium247), isotopeStack(Materials.americium243));
        itemToItem(isotopeIngredient(Materials.berkelium248), dustIngredient(Materials.thorium));
        itemToItem(isotopeIngredient(Materials.californium249), isotopeStack(Materials.curium245));
        itemToItem(isotopeIngredient(Materials.californium250), isotopeStack(Materials.curium246));
        itemToItem(isotopeIngredient(Materials.californium251), isotopeStack(Materials.curium247));
        itemToItem(isotopeIngredient(Materials.californium252), dustIngredient(Materials.thorium));
    }
}
