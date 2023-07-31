package igentuman.nc.setup.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.multiblock.FissionRecipe;
import igentuman.nc.recipes.processors.SmeltingIRecipe;
import igentuman.nc.recipes.serializers.ItemStackToItemStackRecipeSerializer;
import igentuman.nc.setup.processors.Processors;

import java.util.HashMap;


public class NcRecipeSerializers {

    private NcRecipeSerializers() {
    }

    public static final RecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new RecipeSerializerDeferredRegister(NuclearCraft.MODID);

    public static HashMap<String, RecipeSerializerRegistryObject<ItemStackToItemStackRecipe>> SERIALIZERS = initSerializers();

    private static HashMap<String, RecipeSerializerRegistryObject<ItemStackToItemStackRecipe>> initSerializers() {
        HashMap<String, RecipeSerializerRegistryObject<ItemStackToItemStackRecipe>> map = new HashMap<>();
        map.put(FissionControllerBE.NAME, RECIPE_SERIALIZERS.register(FissionControllerBE.NAME, () -> new ItemStackToItemStackRecipeSerializer<>(FissionRecipe::new)));
        for(String key : Processors.all().keySet()) {
           if( Processors.all().get(key).recipeSerializerSupplier != null) {
               map.put(key, RECIPE_SERIALIZERS.register(key, Processors.all().get(key).recipeSerializerSupplier));
           }
        }
        map.put("smelting", RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new)));
        return map;
    }
}