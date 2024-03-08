package igentuman.nc.recipes;

import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface INcRecipeTypeProvider<RECIPE extends NcRecipe> {

    /**
     * Gets the registry name of the element represented by this provider.
     *
     * @return Registry name.
     */
    default ResourceLocation getRegistryName() {
        return getRecipeType().getRegistryName();
    }

    NcRecipeType<RECIPE> getRecipeType();


    @NotNull
    default List<RECIPE> getRecipes(@Nullable Level world) {
        return getRecipeType().getRecipes(world);
    }

    default Stream<RECIPE> stream(@Nullable Level world) {
        return getRecipes(world).stream();
    }
    @Nullable
    default RECIPE findFirst(@Nullable Level world, Predicate<RECIPE> matchCriteria) {
        return stream(world).filter(matchCriteria).findFirst().orElse(null);
    }
    default boolean contains(@Nullable Level world, Predicate<RECIPE> matchCriteria) {
        return stream(world).anyMatch(matchCriteria);
    }
}