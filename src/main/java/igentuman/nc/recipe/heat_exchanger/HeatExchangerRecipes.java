package igentuman.nc.recipe.heat_exchanger;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

public final class HeatExchangerRecipes {

    private HeatExchangerRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<HeatExchangerRecipe>> HX_TYPE =
            RECIPE_TYPES.register("heat_exchanger", () -> RecipeType.simple(rl("heat_exchanger")));
    public static final DeferredHolder<RecipeSerializer<?>, HeatExchangerRecipeSerializer> HX_SERIALIZER =
            RECIPE_SERIALIZERS.register("heat_exchanger", HeatExchangerRecipeSerializer::new);

    public static void init() {}
}
