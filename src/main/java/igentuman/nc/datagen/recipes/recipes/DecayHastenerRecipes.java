package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.IFinishedRecipe;

import java.util.function.Consumer;

public class DecayHastenerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        DecayHastenerRecipes.consumer = consumer;
        ID = Processors.DECAY_HASTENER;

        itemToItem(dustIngredient(Materials.strontium_90), dustStack(Materials.strontium), 2);
        itemToItem(dustIngredient(Materials.thorium, 4), isotopeStack(Materials.thorium232));
        itemToItem(dustIngredient(Materials.radium), dustStack(Materials.lead));
        itemToItem(dustIngredient(Materials.polonium), dustStack(Materials.lead));
        itemToItem(dustIngredient(Materials.protactinium_233), isotopeStack(Materials.uranium233));
        itemToItem(dustIngredient(Materials.bismuth), dustStack(Materials.thallium));
        itemToItem(dustIngredient(Materials.tbp), isotopeStack(Materials.thorium230));
        itemToItem(dustIngredient(Materials.ruthenium_106), dustStack(Materials.palladium));
        itemToItem(dustIngredient(Materials.caesium_137), dustStack(Materials.barium));
        itemToItem(dustIngredient(Materials.promethium_147), dustStack(Materials.neodymium));
        itemToItem(dustIngredient(Materials.europium_155), dustStack(Materials.gadolinium));
        itemToItem(isotopeIngredient(Materials.uranium233), dustStack(Materials.bismuth));
        itemToItem(isotopeIngredient(Materials.uranium235), dustStack(Materials.lead));
        itemToItem(isotopeIngredient(Materials.uranium238), dustStack(Materials.radium));
        itemToItem(isotopeIngredient(Materials.neptunium236), dustStack(Materials.thorium));
        itemToItem(isotopeIngredient(Materials.neptunium237), isotopeStack(Materials.uranium233));
        itemToItem(isotopeIngredient(Materials.plutonium238), dustStack(Materials.lead));
        itemToItem(isotopeIngredient(Materials.plutonium239), isotopeStack(Materials.uranium233));
        itemToItem(isotopeIngredient(Materials.plutonium241), isotopeStack(Materials.neptunium237));
        itemToItem(isotopeIngredient(Materials.plutonium242), isotopeStack(Materials.uranium238));
        itemToItem(isotopeIngredient(Materials.americium241), isotopeStack(Materials.neptunium237));
        itemToItem(isotopeIngredient(Materials.americium242), dustStack(Materials.lead));
        itemToItem(isotopeIngredient(Materials.americium243), isotopeStack(Materials.plutonium238));
        itemToItem(isotopeIngredient(Materials.curium243), isotopeStack(Materials.plutonium239));
        itemToItem(isotopeIngredient(Materials.curium245), isotopeStack(Materials.plutonium241));
        itemToItem(isotopeIngredient(Materials.curium246), isotopeStack(Materials.plutonium242));
        itemToItem(isotopeIngredient(Materials.curium247), isotopeStack(Materials.americium243));
        itemToItem(isotopeIngredient(Materials.berkelium247), isotopeStack(Materials.americium243));
        itemToItem(isotopeIngredient(Materials.berkelium248), dustStack(Materials.thorium));
        itemToItem(isotopeIngredient(Materials.californium249), isotopeStack(Materials.curium245));
        itemToItem(isotopeIngredient(Materials.californium250), isotopeStack(Materials.curium246));
        itemToItem(isotopeIngredient(Materials.californium251), isotopeStack(Materials.curium247));
        itemToItem(isotopeIngredient(Materials.californium252), dustStack(Materials.thorium));
    }
}
