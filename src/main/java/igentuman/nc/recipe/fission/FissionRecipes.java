package igentuman.nc.recipe.fission;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

/** Registers the fission reactor fuel and boiling recipe types and their serializers. */
public final class FissionRecipes {

    private FissionRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<FissionFuelRecipe>> FUEL_TYPE =
            RECIPE_TYPES.register("fission_reactor", () -> RecipeType.simple(rl("fission_reactor")));
    public static final DeferredHolder<RecipeSerializer<?>, FissionFuelRecipeSerializer> FUEL_SERIALIZER =
            RECIPE_SERIALIZERS.register("fission_reactor", FissionFuelRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BoilingRecipe>> BOILING_TYPE =
            RECIPE_TYPES.register("fission_boiling", () -> RecipeType.simple(rl("fission_boiling")));
    public static final DeferredHolder<RecipeSerializer<?>, BoilingRecipeSerializer> BOILING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fission_boiling", BoilingRecipeSerializer::new);

    public static void init() {}
}
