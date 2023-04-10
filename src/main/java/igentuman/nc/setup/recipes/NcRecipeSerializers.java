package igentuman.nc.setup.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.multiblock.FissionRecipe;
import igentuman.nc.recipes.processors.SmeltingIRecipe;
import igentuman.nc.recipes.serializers.ItemStackToItemStackRecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;


public class NcRecipeSerializers {

    private NcRecipeSerializers() {
    }

    public static final RecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new RecipeSerializerDeferredRegister(NuclearCraft.MODID);

    public static final RecipeSerializerRegistryObject<ItemStackToItemStackRecipe> FISSION = RECIPE_SERIALIZERS.register("fission_reactor", () -> new ItemStackToItemStackRecipeSerializer<>(FissionRecipe::new));
    public static final RecipeSerializerRegistryObject<ItemStackToItemStackRecipe> SMELTING = RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new));
}