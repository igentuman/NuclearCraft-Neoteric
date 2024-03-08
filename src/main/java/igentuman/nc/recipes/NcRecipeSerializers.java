package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraft.data.recipes.SpecialRecipeBuilder.special;

public class NcRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, MODID
    );

    private NcRecipeSerializers() {
    }
/*   public static final RegistryObject<SimpleRecipeSerializer<ResetNbtRecipe>> RESET_NBT = RECIPE_SERIALIZERS.register(
            "reset_nbt", special(ResetNbtRecipe::new)
    );
    public static final RegistryObject<SimpleRecipeSerializer<RadShieldingRecipe>> SHIELDING = RECIPE_SERIALIZERS.register(
            "shielding", special(RadShieldingRecipe::new)
    );*/
    public static HashMap<String, RegistryObject<NcRecipeSerializer<? extends NcRecipe>>> SERIALIZERS = initSerializers();

    private static HashMap<String, RegistryObject<NcRecipeSerializer<? extends NcRecipe>>> initSerializers() {
        HashMap<String, RegistryObject<NcRecipeSerializer<? extends NcRecipe>>> map = new HashMap<>();
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
        //map.put("smelting", RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new)));
        return map;
    }
}