package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.RecipeOutput;


import static igentuman.nc.content.materials.Materials.*;

public class DecayHastenerRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        DecayHastenerRecipes.consumer = consumer;
        ID = Processors.DECAY_HASTENER;

        itemToItem(dustIngredient(irradiated_borax), dustIngredient(borax), 2);
        itemToItem(dustIngredient(strontium_90), dustIngredient(strontium), 2);
        itemToItem(dustIngredient(radium), dustIngredient(lead));
        itemToItem(dustIngredient(polonium), dustIngredient(lead));
        itemToItem(dustIngredient(protactinium_233), isotopeStack(uranium233));
        itemToItem(dustIngredient(bismuth), dustIngredient(thallium));
        itemToItem(dustIngredient(tbp), isotopeStack(thorium230));
        itemToItem(dustIngredient(ruthenium_106), dustIngredient(palladium));
        itemToItem(dustIngredient(caesium_137), dustIngredient(barium));
        itemToItem(dustIngredient(promethium_147), dustIngredient(neodymium));
        itemToItem(dustIngredient(europium_155), dustIngredient(gadolinium));
        itemToItem(isotopeIngredient(uranium233), dustIngredient(bismuth));
        itemToItem(isotopeIngredient(uranium235), dustIngredient(lead));
        itemToItem(isotopeIngredient(uranium238), dustIngredient(radium));
        itemToItem(isotopeIngredient(neptunium236), dustIngredient(thorium));
        itemToItem(isotopeIngredient(neptunium237), isotopeStack(uranium233));
        itemToItem(isotopeIngredient(plutonium238), dustIngredient(lead));
        itemToItem(isotopeIngredient(plutonium239), isotopeStack(uranium233));
        itemToItem(isotopeIngredient(plutonium241), isotopeStack(neptunium237));
        itemToItem(isotopeIngredient(plutonium242), isotopeStack(uranium238));
        itemToItem(isotopeIngredient(americium241), isotopeStack(neptunium237));
        itemToItem(isotopeIngredient(americium242), dustIngredient(lead));
        itemToItem(isotopeIngredient(americium243), isotopeStack(plutonium238));
        itemToItem(isotopeIngredient(curium243), isotopeStack(plutonium239));
        itemToItem(isotopeIngredient(curium245), isotopeStack(plutonium241));
        itemToItem(isotopeIngredient(curium246), isotopeStack(americium242));
        itemToItem(isotopeIngredient(curium247), isotopeStack(americium243));
        itemToItem(isotopeIngredient(berkelium247), isotopeStack(americium243));
        itemToItem(isotopeIngredient(berkelium248), dustIngredient(thorium));
        itemToItem(isotopeIngredient(californium249), isotopeStack(curium245));
        itemToItem(isotopeIngredient(californium250), isotopeStack(curium246));
        itemToItem(isotopeIngredient(californium251), isotopeStack(curium247));
        itemToItem(isotopeIngredient(californium252), dustIngredient(thorium));
        itemToItem(isotopeIngredient(xenorium298), isotopeIngredient(quantite));
    }
}
