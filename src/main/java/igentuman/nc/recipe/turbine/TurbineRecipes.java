package igentuman.nc.recipe.turbine;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

public final class TurbineRecipes {

    private TurbineRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<TurbineRecipe>> TURBINE_TYPE =
            RECIPE_TYPES.register("turbine", () -> RecipeType.simple(rl("turbine")));
    public static final DeferredHolder<RecipeSerializer<?>, TurbineRecipeSerializer> TURBINE_SERIALIZER =
            RECIPE_SERIALIZERS.register("turbine", TurbineRecipeSerializer::new);

    public static void init() {}
}
