package igentuman.nc.recipe.particle;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

public final class ParticleRecipes {

    private ParticleRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<AcceleratorCoolantRecipe>> ACCELERATOR_COOLANT_TYPE =
            RECIPE_TYPES.register("accelerator_coolant", () -> RecipeType.simple(rl("accelerator_coolant")));
    public static final DeferredHolder<RecipeSerializer<?>, AcceleratorCoolantRecipeSerializer> ACCELERATOR_COOLANT_SERIALIZER =
            RECIPE_SERIALIZERS.register("accelerator_coolant", AcceleratorCoolantRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<TargetChamberRecipe>> TARGET_CHAMBER_TYPE =
            RECIPE_TYPES.register("target_chamber", () -> RecipeType.simple(rl("target_chamber")));
    public static final DeferredHolder<RecipeSerializer<?>, TargetChamberRecipeSerializer> TARGET_CHAMBER_SERIALIZER =
            RECIPE_SERIALIZERS.register("target_chamber", TargetChamberRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DecayChamberRecipe>> DECAY_CHAMBER_TYPE =
            RECIPE_TYPES.register("decay_chamber", () -> RecipeType.simple(rl("decay_chamber")));
    public static final DeferredHolder<RecipeSerializer<?>, DecayChamberRecipeSerializer> DECAY_CHAMBER_SERIALIZER =
            RECIPE_SERIALIZERS.register("decay_chamber", DecayChamberRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CollisionChamberRecipe>> COLLISION_CHAMBER_TYPE =
            RECIPE_TYPES.register("collision_chamber", () -> RecipeType.simple(rl("collision_chamber")));
    public static final DeferredHolder<RecipeSerializer<?>, CollisionChamberRecipeSerializer> COLLISION_CHAMBER_SERIALIZER =
            RECIPE_SERIALIZERS.register("collision_chamber", CollisionChamberRecipeSerializer::new);

    public static void init() {}
}
