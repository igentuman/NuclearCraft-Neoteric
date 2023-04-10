package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.NcClient;
import igentuman.nc.recipes.cache.IInputRecipeCache;
import igentuman.nc.recipes.cache.InputRecipeCache;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IItemStackIngredientCreator;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.processors.SmeltingIRecipe;
import igentuman.nc.setup.recipes.RecipeTypeDeferredRegister;
import igentuman.nc.setup.recipes.RecipeTypeRegistryObject;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;

public class NcRecipeType<RECIPE extends NcRecipe, INPUT_CACHE extends IInputRecipeCache> implements RecipeType<RECIPE>,
        INcRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(MODID);

    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> FISSION =
          register("fission_reactor", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> SMELTING =
            register("smelting", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    private static <RECIPE extends NcRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> register(String name,
                                                                                                                                           Function<NcRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        return RECIPE_TYPES.register(name, () -> new NcRecipeType<>(name, inputCacheCreator));
    }

    public static void clearCache() {
        for (INcRecipeTypeProvider<?, ?> recipeTypeProvider : RECIPE_TYPES.getAllRecipeTypes()) {
            recipeTypeProvider.getRecipeType().clearCaches();
        }
    }

    private List<RECIPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;
    private final INPUT_CACHE inputCache;

    private NcRecipeType(String name, Function<NcRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        this.registryName = NuclearCraft.rl(name);
        this.inputCache = inputCacheCreator.apply(this);
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
    public NcRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
        return this;
    }

    private void clearCaches() {
        cachedRecipes = Collections.emptyList();
        inputCache.clear();
    }

    @Override
    public INPUT_CACHE getInputCache() {
        return inputCache;
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
            if (this == SMELTING.get()) {
                //Ensure the recipes can be modified
                recipes = new ArrayList<>(recipes);
                for (SmeltingRecipe smeltingRecipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
                    ItemStack recipeOutput = smeltingRecipe.getResultItem();
                    if (!smeltingRecipe.isSpecial() && !smeltingRecipe.isIncomplete() && !recipeOutput.isEmpty()) {
                        //TODO: Can Smelting recipes even be "special", if so can we add some sort of checker to make getOutput return the correct result
                        NonNullList<Ingredient> ingredients = smeltingRecipe.getIngredients();
                        ItemStackIngredient input;
                        if (ingredients.isEmpty()) {
                            //Something went wrong
                            continue;
                        } else {
                            IItemStackIngredientCreator ingredientCreator = IngredientCreatorAccess.item();
                            input = ingredientCreator.from(ingredients.stream().map(ingredientCreator::from));
                        }
                        recipes.add((RECIPE) new SmeltingIRecipe(smeltingRecipe.getId(), input, recipeOutput));
                    }
                }
            }
            //Make the list of cached recipes immutable and filter out any incomplete recipes
            // as there is no reason to potentially look the partial complete piece up if
            // the other portion of the recipe is incomplete
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