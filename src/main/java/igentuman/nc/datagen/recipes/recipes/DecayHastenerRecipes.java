package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class DecayHastenerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        DecayHastenerRecipes.consumer = consumer;
        ID = Processors.DECAY_HASTENER;
        add(dustItem(Materials.thorium), dustItem(Materials.lead));
        add(dustItem(Materials.radium), dustItem(Materials.lead));
        add(dustItem(Materials.polonium), dustItem(Materials.lead));
        add(dustItem(Materials.protactinium_233), isotopeItem("uranium/233"));
        add(dustItem(Materials.bismuth), dustItem(Materials.thallium));
        add(dustItem(Materials.tbp), isotopeItem("thorium/230"));
        add(dustItem(Materials.strontium_90), dustItem(Materials.zirconium));
        add(dustItem(Materials.ruthenium_106), dustItem(Materials.palladium));
        add(dustItem(Materials.caesium_137), dustItem(Materials.barium));
        add(dustItem(Materials.promethium_147), dustItem(Materials.neodymium));
        add(dustItem(Materials.europium_155), dustItem(Materials.gadolinium));
        add(isotopeItem("uranium/233"), dustItem(Materials.bismuth));
        add(isotopeItem("uranium/235"), dustItem(Materials.lead));
        add(isotopeItem("uranium/238"), dustItem(Materials.radium));
        add(isotopeItem("neptunium/236"), dustItem(Materials.thorium));
        add(isotopeItem("neptunium/237"), isotopeItem("uranium/233"));
        add(isotopeItem("plutonium/238"), dustItem(Materials.lead));
        add(isotopeItem("plutonium/239"), isotopeItem("uranium/233"));
        add(isotopeItem("plutonium/241"), isotopeItem("neptunium/237"));
        add(isotopeItem("plutonium/242"), isotopeItem("uranium/238"));
        add(isotopeItem("americium/241"), isotopeItem("neptunium/237"));
        add(isotopeItem("americium/242"), dustItem(Materials.lead));
        add(isotopeItem("americium/243"), isotopeItem("plutonium/239"));
        add(isotopeItem("curium/243"), isotopeItem("plutonium/239"));
        add(isotopeItem("curium/245"), isotopeItem("plutonium/241"));
        add(isotopeItem("curium/246"), isotopeItem("plutonium/242"));
        add(isotopeItem("curium/247"), isotopeItem("americium/243"));
        add(isotopeItem("berkelium/247"), isotopeItem("americium/243"));
        add(isotopeItem("berkelium/248"), dustItem(Materials.thorium));
        add(isotopeItem("californium/249"), isotopeItem("curium/245"));
        add(isotopeItem("californium/250"), isotopeItem("curium/246"));
        add(isotopeItem("californium/251"), isotopeItem("curium/247"));
        add(isotopeItem("californium/252"), dustItem(Materials.thorium));
    }
}
