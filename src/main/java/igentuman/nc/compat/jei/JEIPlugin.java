package igentuman.nc.compat.jei;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.OreVeinRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.compat.GlobalVars.*;

@JeiPlugin
public  class JEIPlugin implements IModPlugin {
    public  static HashMap<String, RecipeType<? extends AbstractRecipe>>  recipeTypes;

    public static final RecipeType<FissionControllerBE.Recipe> FISSION = new RecipeType<>(new ResourceLocation(MODID, FissionControllerBE.NAME), FissionControllerBE.Recipe.class);
    public static final RecipeType<OreVeinRecipe> ORE_VEINS = new RecipeType<>(new ResourceLocation(MODID, "nc_ore_veins"), OreVeinRecipe.class);
    private static HashMap<String, RecipeType<? extends AbstractRecipe>> getRecipeTypes() {
        if(recipeTypes == null) {
            recipeTypes = new HashMap<>();
            for (String name : RECIPE_CLASSES.keySet()) {
                recipeTypes.put(name, new RecipeType<AbstractRecipe>(new ResourceLocation(MODID, name), RECIPE_CLASSES.get(name)));
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
        registration.addRecipeCategories(new OreVeinCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), ORE_VEINS));
        registration.addRecipeCategories(new FissionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION));
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(String name) {
        return (RecipeType<TYPE>) recipeTypes.get(name);
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(RecipeType<? extends AbstractRecipe> in) {
        return (RecipeType<TYPE>) in;
    }


    public void registerRecipes(IRecipeRegistration registration) {
        for(String  name: getRecipeTypes().keySet()) {
            registration.addRecipes(
                    getRecipeType(name),
                    NcRecipeType.ALL_RECIPES.get(name).getRecipes(NcClient.tryGetClientWorld()));
        }
        registration.addRecipes(
                getRecipeType(FISSION),
                NcRecipeType.ALL_RECIPES.get(FissionControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
        registration.addRecipes(
                getRecipeType(ORE_VEINS),
                NcRecipeType.ALL_RECIPES.get("nc_ore_veins").getRecipes(NcClient.tryGetClientWorld()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        for(String  name: getRecipeTypes().keySet()) {
            if(!CATALYSTS.containsKey(name)) continue;
            for(ItemStack stack: CATALYSTS.get(name)) {
                registry.addRecipeCatalyst(stack, getRecipeType(name));
            }
        }
        if(CATALYSTS.containsKey(FissionControllerBE.NAME)) {
            registry.addRecipeCatalyst(CATALYSTS.get(FissionControllerBE.NAME).get(0), FISSION);
        }
        if(CATALYSTS.containsKey("nc_ore_veins")) {
            registry.addRecipeCatalyst(CATALYSTS.get("nc_ore_veins").get(0), ORE_VEINS);
        }
    }
}
