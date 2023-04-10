package igentuman.nc.recipes.cache;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.type.IInputCache;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.util.functions.FourPredicate;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class FourInputRecipeCache<
        INPUT_A, INGREDIENT_A extends InputIngredient<INPUT_A>,
        INPUT_B, INGREDIENT_B extends InputIngredient<INPUT_B>,
        INPUT_C, INGREDIENT_C extends InputIngredient<INPUT_C>,
        INPUT_D, INGREDIENT_D extends InputIngredient<INPUT_D>,
        RECIPE extends NcRecipe & FourPredicate<INPUT_A, INPUT_B, INPUT_C, INPUT_D>,
        CACHE_A extends IInputCache<INPUT_A, INGREDIENT_A, RECIPE>,
        CACHE_B extends IInputCache<INPUT_B, INGREDIENT_B, RECIPE>,
        CACHE_C extends IInputCache<INPUT_C, INGREDIENT_C, RECIPE>,
        CACHE_D extends IInputCache<INPUT_D, INGREDIENT_D, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RECIPE> complexIngredientA = new HashSet<>();
    private final Set<RECIPE> complexIngredientB = new HashSet<>();
    private final Set<RECIPE> complexIngredientC = new HashSet<>();
    private final Set<RECIPE> complexIngredientD = new HashSet<>();
    private final Set<RECIPE> complexRecipes = new HashSet<>();

    private final Function<RECIPE, INGREDIENT_A> inputAExtractor;
    private final Function<RECIPE, INGREDIENT_B> inputBExtractor;
    private final Function<RECIPE, INGREDIENT_C> inputCExtractor;
    private final Function<RECIPE, INGREDIENT_D> inputDExtractor;

    private final CACHE_A cacheA;
    private final CACHE_B cacheB;
    private final CACHE_C cacheC;
    private final CACHE_D cacheD;

    protected FourInputRecipeCache(NcRecipeType<RECIPE, ?> recipeType,
                                   Function<RECIPE, INGREDIENT_A> inputAExtractor, CACHE_A cacheA,
                                   Function<RECIPE, INGREDIENT_B> inputBExtractor, CACHE_B cacheB,
                                   Function<RECIPE, INGREDIENT_C> inputCExtractor, CACHE_C cacheC,
                                   Function<RECIPE, INGREDIENT_D> inputDExtractor, CACHE_D cacheD
    ) {
        super(recipeType);
        this.inputAExtractor = inputAExtractor;
        this.inputBExtractor = inputBExtractor;
        this.inputCExtractor = inputCExtractor;
        this.inputDExtractor = inputDExtractor;
        this.cacheA = cacheA;
        this.cacheB = cacheB;
        this.cacheC = cacheC;
        this.cacheD = cacheD;
    }

    @Override
    public void clear() {
        super.clear();
        cacheA.clear();
        cacheB.clear();
        cacheC.clear();
        cacheD.clear();
        complexIngredientA.clear();
        complexIngredientB.clear();
        complexIngredientC.clear();
        complexIngredientD.clear();
        complexRecipes.clear();
    }

    public boolean containsInputA(@Nullable Level world, INPUT_A input) {
        return containsInput(world, input, inputAExtractor, cacheA, complexIngredientA);
    }

    public boolean containsInputB(@Nullable Level world, INPUT_B input) {
        return containsInput(world, input, inputBExtractor, cacheB, complexIngredientB);
    }

    public boolean containsInputC(@Nullable Level world, INPUT_C input) {
        return containsInput(world, input, inputCExtractor, cacheC, complexIngredientC);
    }

    public boolean containsInputD(@Nullable Level world, INPUT_D input) {
        return containsInput(world, input, inputDExtractor, cacheD, complexIngredientD);
    }

    /**
     * Finds the first recipe that matches the given inputs.
     *
     * @param world  World.
     * @param inputA Recipe input A.
     * @param inputB Recipe input B.
     * @param inputC Recipe input C.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     *
     * @implNote Lookups up the recipe first from the A input map (the fact that it is A is arbitrary and just as well could be B or C).
     */
    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, INPUT_C inputC, INPUT_D inputD) {
        if (cacheA.isEmpty(inputA) || cacheB.isEmpty(inputB)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        Predicate<RECIPE> matchPredicate = r -> r.test(inputA, inputB, inputC, inputD);
        //Lookup a recipe from the A input map (the fact that it is A is arbitrary, it just as well could be B or C)
        RECIPE recipe = cacheA.findFirstRecipe(inputA, matchPredicate);
        // if there is no recipe, then check if any of our complex recipes (either a, b, or c being complex) match
        return recipe == null ? findFirstRecipe(complexRecipes, matchPredicate) : recipe;
    }

    @Override
    protected void initCache(List<RECIPE> recipes) {
        for (RECIPE recipe : recipes) {
            boolean complexA = cacheA.mapInputs(recipe, inputAExtractor.apply(recipe));
            boolean complexB = cacheB.mapInputs(recipe, inputBExtractor.apply(recipe));
            boolean complexC = cacheC.mapInputs(recipe, inputCExtractor.apply(recipe));
            boolean complexD = cacheD.mapInputs(recipe, inputDExtractor.apply(recipe));
            if (complexA) {
                complexIngredientA.add(recipe);
            }
            if (complexB) {
                complexIngredientB.add(recipe);
            }
            if (complexC) {
                complexIngredientC.add(recipe);
            }
            if (complexD) {
                complexIngredientD.add(recipe);
            }
            if (complexA || complexB || complexC || complexD) {
                complexRecipes.add(recipe);
            }
        }
    }
}