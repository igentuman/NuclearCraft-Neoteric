package igentuman.nc.setup.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.recipes.shielding.RadShieldingRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.serializers.NcRecipeSerializer;
import igentuman.nc.content.processors.Processors;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

import java.util.HashMap;

public class NcRecipeSerializers {

    private NcRecipeSerializers() {
    }

    public static final RecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new RecipeSerializerDeferredRegister(NuclearCraft.MODID);
    public static final RecipeSerializerRegistryObject<RadShieldingRecipe> SHIELDING = RECIPE_SERIALIZERS.register("shielding", () -> new SimpleRecipeSerializer<>(RadShieldingRecipe::new));

    public static HashMap<String, RecipeSerializerRegistryObject<NcRecipe>> SERIALIZERS = initSerializers();

    private static HashMap<String, RecipeSerializerRegistryObject<NcRecipe>> initSerializers() {
        HashMap<String, RecipeSerializerRegistryObject<NcRecipe>> map = new HashMap<>();
        map.put(FissionControllerBE.NAME, RECIPE_SERIALIZERS.register(FissionControllerBE.NAME, () -> new NcRecipeSerializer<>(FissionControllerBE.Recipe::new)));
        for(String key : Processors.all().keySet()) {
           if( Processors.all().get(key).getRecipeSerializer() != null) {
               map.put(key, RECIPE_SERIALIZERS.register(key, Processors.all().get(key).getRecipeSerializer()));
           }
        }
        //map.put("smelting", RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new)));
        return map;
    }
}