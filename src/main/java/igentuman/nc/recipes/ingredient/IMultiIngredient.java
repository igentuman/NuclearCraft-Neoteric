package igentuman.nc.recipes.ingredient;

import org.antlr.v4.runtime.misc.NotNull;;

import java.util.List;
import java.util.function.Predicate;

public interface IMultiIngredient<TYPE, INGREDIENT extends InputIngredient<TYPE>> extends InputIngredient<TYPE> {

    /**
     * For use in recipe input caching, checks all ingredients even if some match.
     *
     * @return {@code true} if any ingredient matches.
     */
    boolean forEachIngredient(Predicate<INGREDIENT> checker);

    /**
     * @apiNote For use in flattening multi ingredients, this should return an immutable view.
     */
    List<INGREDIENT> getIngredients();
}