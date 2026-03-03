package igentuman.nc.handler.event.client;

import igentuman.nc.recipes.NcRecipeType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.recipes.NcRecipeType.ALL_RECIPES;

public class TagsUpdated {
    public static void register(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(TagsUpdated::tagsUpated);
    }
    public static void tagsUpated(TagsUpdatedEvent event) {
        if(RecipesUpdated.manager != null) {
            for (String name: ALL_RECIPES.keySet()) {
                NcRecipeType recipeType = ALL_RECIPES.get(name).getRecipeType();
                //recipeType.loadRecipes(RecipesUpdated.manager);
            }
        }
    }
}
