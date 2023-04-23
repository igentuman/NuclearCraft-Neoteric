package igentuman.nc.recipes.cache;

import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.type.IInputCache;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.util.functions.ConstantPredicates;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Basic implementation for {@link IInputRecipeCache} for handling recipes with a single input.
 */
public abstract class SingleInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends NcRecipe & Predicate<INPUT>,
      CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RECIPE> complexRecipes = new HashSet<>();
    private final Function<RECIPE, INGREDIENT> inputExtractor;
    private final CACHE cache;

    protected SingleInputRecipeCache(NcRecipeType<RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputExtractor, CACHE cache) {
        super(recipeType);
        this.inputExtractor = inputExtractor;
        this.cache = cache;
    }

    @Override
    public void clear() {
        super.clear();
        cache.clear();
        complexRecipes.clear();
    }

    /**
     * Checks if there is a matching recipe that has the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInput(@Nullable Level world, INPUT input) {
        return containsInput(world, input, inputExtractor, cache, complexRecipes);
    }

    /**
     * Finds the first recipe that matches the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, INPUT input) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        Predicate<RECIPE> matchPredicate = recipe -> recipe.test(input);
        RECIPE recipe = cache.findFirstRecipe(input, matchPredicate);
        return recipe == null ? findFirstRecipe(complexRecipes, matchPredicate) : recipe;
    }

    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, ItemCapabilityHandler input) {
/*        if (cache.isEmpty((INPUT) ItemStackIngredientCreator.INSTANCE.from(input.getStackInSlot(0)))) {
            //Don't allow empty inputs
            return null;
        }*/
        initCacheIfNeeded(world);
        Predicate<RECIPE> matchPredicate = recipe -> recipe.test((INPUT) input.getStackInSlot(0));
        return findFirstRecipe(complexRecipes, matchPredicate);
    }

    /**
     * Finds the first recipe that matches the given input type ignoring the size requirement.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input) {
        return findTypeBasedRecipe(world, input, ConstantPredicates.alwaysTrue());
    }

    /**
     * Finds the first recipe that matches the given input type ignoring the size requirement and also matches the given recipe predicate.
     *
     * @param world         World.
     * @param input         Recipe input.
     * @param matchCriteria Extra validation criteria to check.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input, Predicate<RECIPE> matchCriteria) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        RECIPE recipe = cache.findFirstRecipe(input, matchCriteria);
        return recipe == null ? findFirstRecipe(complexRecipes, r -> inputExtractor.apply(r).testType(input) && matchCriteria.test(r)) : recipe;
    }

    @Override
    protected void initCache(List<RECIPE> recipes) {
        complexRecipes.addAll(recipes);
    }
}