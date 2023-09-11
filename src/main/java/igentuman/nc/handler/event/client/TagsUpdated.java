package igentuman.nc.handler.event.client;

import igentuman.nc.recipes.NcRecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.recipes.NcRecipeType.ALL_RECIPES;

public class TagsUpdated {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TagsUpdated::tagsUpated);
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
