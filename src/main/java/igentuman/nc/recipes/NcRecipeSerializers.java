package igentuman.nc.recipes;

import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE.FusionCoolantRecipe;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import igentuman.nc.recipes.type.NuclearBlastRecipe;
import igentuman.nc.recipes.serializers.NuclearBlastRecipeSerializer;
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
    public static final RecipeSerializerRegistryObject<NuclearBlastRecipe> NUCLEAR_BLAST_RECIPE = RECIPE_SERIALIZERS.register("nuclear_blast", () -> new NuclearBlastRecipeSerializer());

    public static final HashMap<String, RecipeSerializerRegistryObject<? extends NcRecipe>> SERIALIZERS = initSerializers();

    private static HashMap<String, RecipeSerializerRegistryObject<? extends NcRecipe>> initSerializers() {
        HashMap<String, RecipeSerializerRegistryObject<? extends NcRecipe>> map = new HashMap<>();
        map.put("fusion_core", RECIPE_SERIALIZERS.register("fusion_core", () -> new FusionRecipeSerializer<>(FusionCoreBE.Recipe::new)));
        map.put(MSRControllerBE.NAME, RECIPE_SERIALIZERS.register(MSRControllerBE.NAME, () -> new NcRecipeSerializer<>(MSRControllerBE.Recipe::new)));
        map.put(FissionControllerBE.NAME, RECIPE_SERIALIZERS.register(FissionControllerBE.NAME, () -> new NcRecipeSerializer<>(FissionControllerBE.Recipe::new)));
        map.put("kugelblitz_chamber", RECIPE_SERIALIZERS.register("kugelblitz_chamber", () -> new NcRecipeSerializer<>(ChamberTerminalBE.Recipe::new)));
        map.put("nc_ore_veins", RECIPE_SERIALIZERS.register("nc_ore_veins", () -> new OreVeinRecipeSerializer<>(OreVeinRecipe::new)));
        map.put("accelerator_coolant", RECIPE_SERIALIZERS.register("accelerator_coolant", () -> new CoolantRecipeSerializer<>(LinearAcceleratorControllerBE.CoolantRecipe::new)));
        map.put("fusion_coolant", RECIPE_SERIALIZERS.register("fusion_coolant", () -> new CoolantRecipeSerializer<>(FusionCoolantRecipe::new)));
        map.put("fission_boiling", RECIPE_SERIALIZERS.register("fission_boiling", () -> new BoilingRecipeSerializer<>(FissionControllerBE.FissionBoilingRecipe::new)));
        map.put("target_chamber", RECIPE_SERIALIZERS.register("target_chamber", () -> new TargetChamberSerializer<>(TargetChamberControllerBE.Recipe::new)));
        map.put("decay_chamber", RECIPE_SERIALIZERS.register("decay_chamber", () -> new ParticleOnlyRecipeSerializer<>(DecayChamberControllerBE.Recipe::new)));
        map.put("collision_chamber", RECIPE_SERIALIZERS.register("collision_chamber", () -> new ParticleOnlyRecipeSerializer<>(CollisionChamberControllerBE.Recipe::new)));
        map.put("nuclear_blast", NUCLEAR_BLAST_RECIPE);
        map.put(TurbineControllerBE.NAME, RECIPE_SERIALIZERS.register(TurbineControllerBE.NAME, () -> new TurbineRecipeSerializer<>(TurbineControllerBE.Recipe::new)));
        map.put(HeatExchangerControllerBE.NAME, RECIPE_SERIALIZERS.register(HeatExchangerControllerBE.NAME, () -> new HeatExchangerRecipeSerializer<>(HeatExchangerControllerBE.Recipe::new)));
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