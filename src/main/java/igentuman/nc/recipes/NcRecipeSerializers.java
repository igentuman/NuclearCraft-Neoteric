package igentuman.nc.recipes;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE.FusionCoolantRecipe;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.recipes.serializers.*;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.recipes.type.RadShieldingRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.type.ResetNbtRecipe;
import igentuman.nc.registry.RecipeSerializerRegistryObject;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.HashMap;

import static igentuman.nc.setup.registration.Registries.RECIPE_SERIALIZERS;

public class NcRecipeSerializers {

    private NcRecipeSerializers() {
    }

    public static final RecipeSerializerRegistryObject<RadShieldingRecipe> SHIELDING = RECIPE_SERIALIZERS.register("shielding", () -> new SimpleCraftingRecipeSerializer<>(RadShieldingRecipe::new));
    public static final RecipeSerializerRegistryObject<ResetNbtRecipe> RESET_NBT = RECIPE_SERIALIZERS.register("reset_nbt", () -> new SimpleCraftingRecipeSerializer<>(ResetNbtRecipe::new));

    public static HashMap<String, RecipeSerializerRegistryObject<? extends NcRecipe>> SERIALIZERS = initSerializers();

    private static HashMap<String, RecipeSerializerRegistryObject<? extends NcRecipe>> initSerializers() {
        HashMap<String, RecipeSerializerRegistryObject<? extends NcRecipe>> map = new HashMap<>();
        map.put("fusion_core", RECIPE_SERIALIZERS.register("fusion_core", () -> new FusionRecipeSerializer<>(FusionCoreBE.Recipe::new)));
        map.put(FissionControllerBE.NAME, RECIPE_SERIALIZERS.register(FissionControllerBE.NAME, () -> new NcRecipeSerializer<>(FissionControllerBE.Recipe::new)));
        map.put("nc_ore_veins", RECIPE_SERIALIZERS.register("nc_ore_veins", () -> new OreVeinRecipeSerializer<>(OreVeinRecipe::new)));
        map.put("fusion_coolant", RECIPE_SERIALIZERS.register("fusion_coolant", () -> new CoolantRecipeSerializer<>(FusionCoolantRecipe::new)));
        map.put("fission_boiling", RECIPE_SERIALIZERS.register("fission_boiling", () -> new BoilingRecipeSerializer<>(FissionControllerBE.FissionBoilingRecipe::new)));
        map.put(TurbineControllerBE.NAME, RECIPE_SERIALIZERS.register(TurbineControllerBE.NAME, () -> new TurbineRecipeSerializer<>(TurbineControllerBE.Recipe::new)));
        for(String key : Processors.all().keySet()) {
           if(Processors.all().get(key).getRecipeSerializer() != null) {
               map.put(key, RECIPE_SERIALIZERS.register(key, Processors.all().get(key).getRecipeSerializer()));
           }
        }
        return map;
    }

    public static void init() {

    }
}