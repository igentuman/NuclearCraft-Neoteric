package igentuman.nc.recipes;

import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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
    default List<RECIPE> getRecipes(@Nullable World world) {
        return getRecipeType().getRecipes(world);
    }

    default Stream<RECIPE> stream(@Nullable World world) {
        return getRecipes(world).stream();
    }
    @Nullable
    default RECIPE findFirst(@Nullable World world, Predicate<RECIPE> matchCriteria) {
        return stream(world).filter(matchCriteria).findFirst().orElse(null);
    }
    default boolean contains(@Nullable World world, Predicate<RECIPE> matchCriteria) {
        return stream(world).anyMatch(matchCriteria);
    }
}