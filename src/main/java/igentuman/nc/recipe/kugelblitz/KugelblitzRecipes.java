package igentuman.nc.recipe.kugelblitz;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

public final class KugelblitzRecipes {

    private KugelblitzRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<KugelblitzRecipe>> KUGELBLITZ_TYPE =
            RECIPE_TYPES.register("kugelblitz_chamber", () -> RecipeType.simple(rl("kugelblitz_chamber")));
    public static final DeferredHolder<RecipeSerializer<?>, KugelblitzRecipeSerializer> KUGELBLITZ_SERIALIZER =
            RECIPE_SERIALIZERS.register("kugelblitz_chamber", KugelblitzRecipeSerializer::new);

    public static void init() {}
}
