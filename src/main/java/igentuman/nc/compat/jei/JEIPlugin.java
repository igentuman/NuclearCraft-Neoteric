package igentuman.nc.compat.jei;

import igentuman.nc.client.NcClient;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.multiblock.FissionRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.compat.GlobalVars.*;

@JeiPlugin
public  class JEIPlugin implements IModPlugin {
    public  static HashMap<String, RecipeType<NcRecipe>>  recipeTypes;

    public static RecipeType<NcRecipe> FISSION = new RecipeType<NcRecipe>(new ResourceLocation(MODID, "fission_reactor"), FissionRecipe.class);
    private static HashMap<String, RecipeType<NcRecipe>> getRecipeTypes() {
        if(recipeTypes == null) {
            recipeTypes = new HashMap<>();
            for (String name : RECIPE_CLASSES.keySet()) {
                recipeTypes.put(name, new RecipeType<NcRecipe>(new ResourceLocation(MODID, name), RECIPE_CLASSES.get(name)));
            }
        }
        return recipeTypes;
    }


    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MODID, "jei_plugin");
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        for(String  name: getRecipeTypes().keySet()) {
            registration.addRecipeCategories(new ProcessorCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), getRecipeType(name)));
        }
        registration.addRecipeCategories(new FissionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION));
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(String name) {
        return (RecipeType<TYPE>) recipeTypes.get(name);
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(RecipeType<NcRecipe> in) {
        return (RecipeType<TYPE>) in;
    }


    public void registerRecipes(IRecipeRegistration registration) {
        for(String  name: getRecipeTypes().keySet()) {
            registration.addRecipes(
                    getRecipeType(name),
                    NcRecipeType.RECIPES.get(name).getRecipes(NcClient.tryGetClientWorld()));
        }
        registration.addRecipes(
                getRecipeType(FISSION),
                NcRecipeType.RECIPES.get("fission_reactor").getRecipes(NcClient.tryGetClientWorld()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        for(String  name: getRecipeTypes().keySet()) {
            if(!CATALYSTS.containsKey(name)) continue;
            for(ItemStack stack: CATALYSTS.get(name)) {
                registry.addRecipeCatalyst(stack, getRecipeType(name));
            }
        }
        registry.addRecipeCatalyst(CATALYSTS.get("fission_reactor").get(0), FISSION);
    }
}
