package igentuman.nc.recipe.bomb;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.Registers.RECIPE_TYPES;

public final class NcBlastRecipes {

    private NcBlastRecipes() {}

    public static final DeferredHolder<RecipeType<?>, RecipeType<NuclearBlastRecipe>> TYPE =
            RECIPE_TYPES.register("nuclear_blast", () -> RecipeType.simple(rl("nuclear_blast")));
    public static final DeferredHolder<RecipeSerializer<?>, NuclearBlastRecipeSerializer> SERIALIZER =
            RECIPE_SERIALIZERS.register("nuclear_blast", NuclearBlastRecipeSerializer::new);

    public static void init() {}
}
