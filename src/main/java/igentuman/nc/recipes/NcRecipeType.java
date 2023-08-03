package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.recipes.RecipeTypeDeferredRegister;
import igentuman.nc.setup.recipes.RecipeTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static igentuman.nc.NuclearCraft.MODID;

public class NcRecipeType<RECIPE extends NcRecipe> implements RecipeType<RECIPE>,
        INcRecipeTypeProvider<RECIPE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(MODID);

    public static final HashMap<String, RecipeTypeRegistryObject<NcRecipe>> ONE_ITEM_RECIPES = initializeRecipes();
    public static final HashMap<String, RecipeTypeRegistryObject<NcRecipe>> TWO_ITEM_RECIPES = initializeTwoItemRecipes();

    public static HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> ALL_RECIPES;
    private static HashMap<String, RecipeTypeRegistryObject<NcRecipe>> initializeRecipes() {
        HashMap<String, RecipeTypeRegistryObject<NcRecipe>> recipes = new HashMap<>();
        recipes.put(FissionControllerBE.NAME, register(FissionControllerBE.NAME));
        recipes.put(Processors.MANUFACTORY, register(Processors.MANUFACTORY));
        recipes.put(Processors.PRESSURIZER, register(Processors.PRESSURIZER));
        recipes.put(Processors.DECAY_HASTENER, register(Processors.DECAY_HASTENER));
        //recipes.put("smelting", SMELTING);
        if(ALL_RECIPES == null) {
            ALL_RECIPES = new HashMap<>();
        }
        for (String name: recipes.keySet()) {
            ALL_RECIPES.put(name, recipes.get(name));
        }
        return recipes;
    }

    private static HashMap<String, RecipeTypeRegistryObject<NcRecipe>> initializeTwoItemRecipes() {
        HashMap<String, RecipeTypeRegistryObject<NcRecipe>> recipes = new HashMap<>();
        recipes.put(Processors.ALLOY_SMELTER, register(Processors.ALLOY_SMELTER));
        if(ALL_RECIPES == null) {
            ALL_RECIPES = new HashMap<>();
        }
        for (String name: recipes.keySet()) {
            ALL_RECIPES.put(name, recipes.get(name));
        }
        return recipes;
    }

  //  public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe> SMELTING =
 //           register("smelting", recipeType -> new ItemStackToItemStackRecipe(recipeType));

    public static <RECIPE extends NcRecipe> RecipeTypeRegistryObject<RECIPE> register(String name) {
        return RECIPE_TYPES.register(name, () -> new NcRecipeType<>(name));
    }


    private List<RECIPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;

    private NcRecipeType(String name) {
        this.registryName = NuclearCraft.rl(name);
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public NcRecipeType<RECIPE> getRecipeType() {
        return this;
    }


    @NotNull
    @Override
    public List<RECIPE> getRecipes(@Nullable Level world) {
        if (world == null) {
            //Try to get a fallback world if we are in a context that may not have one
            //If we are on the client get the client's world, if we are on the server get the current server's world
            world = DistExecutor.unsafeRunForDist(() -> NcClient::tryGetClientWorld, () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
            if (world == null) {
                //If we failed, then return no recipes
                return Collections.emptyList();
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            //Note: This is a fresh mutable list that gets returned
            List<RECIPE> recipes = recipeManager.getAllRecipesFor(this);

            cachedRecipes = recipes.stream()
                  .filter(recipe -> !recipe.isIncomplete())
                  .toList();
        }
        return cachedRecipes;
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static <C extends Container, RECIPE_TYPE extends Recipe<C>> Optional<RECIPE_TYPE> getRecipeFor(RecipeType<RECIPE_TYPE> recipeType, C inventory, Level level) {
        //Only allow looking up complete recipes
        return level.getRecipeManager().getRecipeFor(recipeType, inventory, level)
              .filter(recipe -> !recipe.isIncomplete());
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static Optional<? extends Recipe<?>> byKey(Level level, ResourceLocation id) {
        //Only allow looking up complete recipes
        return level.getRecipeManager().byKey(id)
              .filter(recipe -> !recipe.isIncomplete());
    }
}