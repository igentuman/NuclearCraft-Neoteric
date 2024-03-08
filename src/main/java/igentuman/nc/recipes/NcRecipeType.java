package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.*;

import static igentuman.nc.NuclearCraft.rl;

public class NcRecipeType<RECIPE extends NcRecipe> implements RecipeType<RECIPE>,
        INcRecipeTypeProvider<RECIPE> {


    public static final HashMap<String, RecipeType<? extends NcRecipe>> ALL_RECIPES = initializeRecipes();
    private static HashMap<String, RecipeType<? extends NcRecipe>> initializeRecipes() {
        HashMap<String, RecipeType<? extends NcRecipe>> recipes = new HashMap<>();
        recipes.put(FissionControllerBE.NAME, registerRecipeType(FissionControllerBE.NAME));
        recipes.put("nc_ore_veins", registerRecipeType("nc_ore_veins"));
        recipes.put("fusion_core", registerRecipeType("fusion_core"));
        recipes.put("fusion_coolant", registerRecipeType("fusion_coolant"));
        recipes.put("fission_boiling", registerRecipeType("fission_boiling"));
        recipes.put(TurbineControllerBE.NAME, registerRecipeType(TurbineControllerBE.NAME));

        for(String processorName: Processors.registered().keySet()) {
            if(Processors.registered().get(processorName).hasRecipes()) {
                recipes.put(processorName, registerRecipeType(processorName));
            }
        }

        //recipes.put("smelting", SMELTING);
        return recipes;
    }

  //  public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe> SMELTING =
 //           register("smelting", recipeType -> new ItemStackToItemStackRecipe(recipeType));


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

    private static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String path)
    {
        ResourceLocation name = rl(path);
        return Registry.register(Registry.RECIPE_TYPE, name, new RecipeType<T>()
        {
            @Override
            public String toString()
            {
                return name.toString();
            }
        });
    }

    @NotNull
    @Override
    public List<RECIPE> getRecipes(@Nullable Level world) {
        if (world == null) {
            world = DistExecutor.unsafeRunForDist(() -> NcClient::tryGetClientWorld, () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
            if (world == null) {
                return cachedRecipes;
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
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
        return level.getRecipeManager().getRecipeFor(recipeType, inventory, level)
              .filter(recipe -> !recipe.isIncomplete());
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static Optional<? extends Recipe<?>> byKey(Level level, ResourceLocation id) {
        return level.getRecipeManager().byKey(id)
              .filter(recipe -> !recipe.isIncomplete());
    }
    public boolean isLoaded = false;
    public void loadRecipes(Level level) {
        if(isLoaded) return;
        getRecipes(level);
        isLoaded = true;
    }

    public void loadRecipes(RecipeManager manager) {
        getRecipes(manager);
        isLoaded = true;
    }

    private void getRecipes(RecipeManager manager) {
        List<RECIPE> recipes = manager.getAllRecipesFor(this);
        cachedRecipes = recipes.stream()
                .filter(recipe -> !recipe.isIncomplete())
                .toList();
    }
}