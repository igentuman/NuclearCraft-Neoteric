package igentuman.nc.setup.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.multiblock.FissionRecipe;
import igentuman.nc.recipes.processors.DecayHastenerRecipe;
import igentuman.nc.recipes.processors.ManufactoryRecipe;
import igentuman.nc.recipes.processors.PressurizerRecipe;
import igentuman.nc.recipes.processors.SmeltingIRecipe;
import igentuman.nc.recipes.serializers.ItemStackToItemStackRecipeSerializer;

import java.util.HashMap;


public class NcRecipeSerializers {

    private NcRecipeSerializers() {
    }

    public static final RecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new RecipeSerializerDeferredRegister(NuclearCraft.MODID);

    public static HashMap<String, RecipeSerializerRegistryObject<ItemStackToItemStackRecipe>> SERIALIZERS = initSerializers();

    private static HashMap<String, RecipeSerializerRegistryObject<ItemStackToItemStackRecipe>> initSerializers() {
        HashMap<String, RecipeSerializerRegistryObject<ItemStackToItemStackRecipe>> map = new HashMap<>();
        map.put("fission_reactor", RECIPE_SERIALIZERS.register("fission_reactor", () -> new ItemStackToItemStackRecipeSerializer<>(FissionRecipe::new)));
        map.put("manufactory", RECIPE_SERIALIZERS.register("manufactory", () -> new ItemStackToItemStackRecipeSerializer<>(ManufactoryRecipe::new)));
        map.put("pressurizer", RECIPE_SERIALIZERS.register("pressurizer", () -> new ItemStackToItemStackRecipeSerializer<>(PressurizerRecipe::new)));
        map.put("decay_hastener", RECIPE_SERIALIZERS.register("decay_hastener", () -> new ItemStackToItemStackRecipeSerializer<>(DecayHastenerRecipe::new)));
        map.put("smelting", RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new)));
        return map;
    }
}