package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.compat.jei.RecipeType;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.antlr.v4.runtime.misc.NotNull;
import javax.annotation.Nullable;

import java.util.*;

import static igentuman.nc.NuclearCraft.rl;

public class NcRecipeType<RECIPE extends NcRecipe> implements IRecipeType<RECIPE>,
        INcRecipeTypeProvider<RECIPE> {


    public static final HashMap<String, IRecipeType<? extends NcRecipe>> ALL_RECIPES = initializeRecipes();
    private static HashMap<String, IRecipeType<? extends NcRecipe>> initializeRecipes() {
        HashMap<String, IRecipeType<? extends NcRecipe>> recipes = new HashMap<>();
        //recipes.put(FissionControllerBE.NAME, registerRecipeType(FissionControllerBE.NAME));
    //    recipes.put("nc_ore_veins", registerRecipeType("nc_ore_veins"));
     //   recipes.put("fusion_core", registerRecipeType("fusion_core"));
     //   recipes.put("fusion_coolant", registerRecipeType("fusion_coolant"));
     //   recipes.put("fission_boiling", registerRecipeType("fission_boiling"));
     //   recipes.put(TurbineControllerBE.NAME, registerRecipeType(TurbineControllerBE.NAME));

        for(String processorName: Processors.registered().keySet()) {
            if(Processors.registered().get(processorName).hasRecipes()) {
       //         recipes.put(processorName, registerRecipeType(processorName));
            }
        }

        //recipes.put("smelting", SMELTING);
        return recipes;
    }

  //  public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe> SMELTING =
 //           register("smelting", recipeType -> new ItemStackToItemStackRecipe(recipeType));


    private List<Object> cachedRecipes = Collections.emptyList();
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

    private static  <V, T extends V> T registerRecipeType(String path)
    {
        ResourceLocation name = rl(path);
        return (T) Registry.register(Registry.RECIPE_TYPE, name, null);
    }

    @NotNull
    @Override
    public List<RECIPE> getRecipes(@Nullable World world) {
        if (world == null) {
            world = DistExecutor.unsafeRunForDist(() -> NcClient::tryGetClientWorld, () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
            if (world == null) {
            //    return cachedRecipes;
                return null;
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            List<RECIPE> recipes = recipeManager.getAllRecipesFor(this);
            cachedRecipes = Arrays.asList(recipes.stream()
                    .filter(recipe -> !recipe.isIncomplete())
                    .toArray());
        }
        //    return cachedRecipes;
        return null;
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static <C extends Container & IInventory, RECIPE_TYPE extends IRecipe<C>> Optional<RECIPE_TYPE> getRecipeFor(RecipeType<RECIPE_TYPE> recipeType, C inventory, World level) {
        /*return level.getRecipeManager().getRecipeFor(recipeType, inventory, level)
              .filter(recipe -> !recipe.isSpecial());*/
        return null;
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static Optional<? extends IRecipe<?>> byKey(World level, ResourceLocation id) {
        return level.getRecipeManager().byKey(id)
              .filter(recipe -> !recipe.isSpecial());
    }
    public boolean isLoaded = false;
    public void loadRecipes(World level) {
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
        cachedRecipes = Arrays.asList(recipes.stream()
                .filter(recipe -> !recipe.isIncomplete())
                .toArray());
    }
}