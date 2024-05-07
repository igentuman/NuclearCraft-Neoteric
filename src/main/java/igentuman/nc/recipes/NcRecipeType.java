package igentuman.nc.recipes;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.processor.NuclearFurnaceBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.registry.RecipeTypeDeferredRegister;
import igentuman.nc.registry.RecipeTypeRegistryObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class NcRecipeType<RECIPE extends NcRecipe> implements RecipeType<RECIPE>,
        INcRecipeTypeProvider<RECIPE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(MODID);
    public static boolean initialized = false;
    public static final HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> ALL_RECIPES = initializeRecipes();
    private static HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> initializeRecipes() {
        HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> recipes = new HashMap<>();
        recipes.put(FissionControllerBE.NAME, register(FissionControllerBE.NAME));
        recipes.put("nc_ore_veins", register("nc_ore_veins"));
        recipes.put("fusion_core", register("fusion_core"));
        recipes.put("fusion_coolant", register("fusion_coolant"));
        recipes.put("fission_boiling", register("fission_boiling"));
        recipes.put(TurbineControllerBE.NAME, register(TurbineControllerBE.NAME));

        for(String processorName: Processors.registered().keySet()) {
            if(Processors.registered().get(processorName).hasRecipes()) {
                recipes.put(processorName, register(processorName));
            }
        }
        initialized = true;
        return recipes;
    }

    public static <RECIPE extends NcRecipe> RecipeTypeRegistryObject<RECIPE> register(String name) {
        return RECIPE_TYPES.register(name, () -> new NcRecipeType<>(name));
    }
    private List<RECIPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;

    private NcRecipeType(String name) {
        this.registryName = rl(name);
    }

    public static void invalidateCache() {
        if(!initialized) return;
        for (RecipeTypeRegistryObject<? extends NcRecipe> recipeType : ALL_RECIPES.values()) {
            recipeType.getRecipeType().cachedRecipes = Collections.emptyList();
        }
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
            world = DistExecutor.unsafeRunForDist(() -> NcClient::tryGetClientWorld, () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
            if (world == null) {
                return cachedRecipes;
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            List<RECIPE> recipes = new ArrayList<>();
            if(this.registryName.getPath().equals("nuclear_furnace")) {
                recipes = getSmeltingRecipes(recipeManager);
            } else {
                recipes = recipeManager.getAllRecipesFor(this);
            }
            cachedRecipes = recipes.stream()
                    .filter(recipe -> !recipe.isIncomplete())
                    .toList();
        }
        return cachedRecipes;
    }

    private List<RECIPE> getSmeltingRecipes(RecipeManager recipeManager) {
        List<SmeltingRecipe> smelting = recipeManager.getAllRecipesFor(SMELTING);
        List<RECIPE> recipes = new ArrayList<>();
        for(SmeltingRecipe recipe: smelting) {
            if(recipe.isIncomplete()) {
                continue;
            }
            ItemStackIngredient output = IngredientCreatorAccess.item().from(recipe.getResultItem());
            recipes.add((RECIPE) new NuclearFurnaceBE.Recipe(
                    rl(output.getName()),
                    new ItemStackIngredient[]{IngredientCreatorAccess.item().from(recipe.getIngredients().get(0))},
                    new ItemStack[]{recipe.getResultItem()},
                    new FluidStackIngredient[0],
                    new FluidStack[0],
                    recipe.getCookingTime()/1000D, 1, 1, 1));
        }
        return recipes;
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