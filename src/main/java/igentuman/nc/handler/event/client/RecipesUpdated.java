package igentuman.nc.handler.event.client;

import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.recipes.NcRecipeType.ALL_RECIPES;

public class RecipesUpdated {
    public static void register(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(RecipesUpdated::recipesUpdated);
    }

    public static RecipeManager manager;
    public static void recipesUpdated(RecipesUpdatedEvent event) {
        manager = event.getRecipeManager();
        for (String name: ALL_RECIPES.keySet()) {
            NcRecipeType recipeType = ALL_RECIPES.get(name).getRecipeType();
            //recipeType.loadRecipes(RecipesUpdated.manager);
        }
    }
}
