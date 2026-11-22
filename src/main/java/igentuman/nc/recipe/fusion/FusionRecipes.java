package igentuman.nc.recipe.fusion;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

/** Registers the fusion reactor and fusion coolant recipe types and their serializers. */
public final class FusionRecipes {

    private FusionRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<FusionRecipe>> FUSION_TYPE =
            RECIPE_TYPES.register("fusion_reactor", () -> RecipeType.simple(rl("fusion_reactor")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FusionRecipe>> FUSION_SERIALIZER =
            RECIPE_SERIALIZERS.register("fusion_reactor", FusionRecipeSerializer::create);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FusionCoolantRecipe>> COOLANT_TYPE =
            RECIPE_TYPES.register("fusion_coolant", () -> RecipeType.simple(rl("fusion_coolant")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FusionCoolantRecipe>> COOLANT_SERIALIZER =
            RECIPE_SERIALIZERS.register("fusion_coolant", FusionCoolantRecipeSerializer::create);

    public static void init() {}
}
