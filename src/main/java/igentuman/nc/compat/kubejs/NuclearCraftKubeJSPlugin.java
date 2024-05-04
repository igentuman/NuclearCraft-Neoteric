package igentuman.nc.compat.kubejs;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeOptional;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;

public class NuclearCraftKubeJSPlugin extends KubeJSPlugin {


    RecipeKey<InputFluid[]> INPUT_FLUIDS = FluidComponents.INPUT_ARRAY.key("inputFluids").defaultOptional();
    RecipeKey<InputItem[]> INPUT_ITEMS = ItemComponents.INPUT_ARRAY.key("input").defaultOptional();
    RecipeKey<OutputItem[]> OUTPUT_ITEMS = ItemComponents.OUTPUT_ARRAY.key("output").defaultOptional();
    RecipeKey<OutputFluid[]> OUTPUT_FLUIDS = FluidComponents.OUTPUT_ARRAY.key("outputFluids").defaultOptional();
    RecipeKey<Double> POWER_MODIFIER = NumberComponent.DoubleRange.ANY_DOUBLE.min(-1000).max(1000).key("powerModifier").defaultOptional();
    RecipeKey<Double> TIME_MODIFIER = NumberComponent.DoubleRange.ANY_DOUBLE.min(-1000).max(1000).key("timeModifier").defaultOptional();
    RecipeKey<Double> RADIATION_MODIFIER = NumberComponent.DoubleRange.ANY_DOUBLE.min(-1000).max(1000).key("radiation").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(NCRecipeJS.class, NCRecipeJS::new,
            INPUT_ITEMS, INPUT_FLUIDS,
            OUTPUT_ITEMS, OUTPUT_FLUIDS,
            POWER_MODIFIER, TIME_MODIFIER, RADIATION_MODIFIER, RADIATION_MODIFIER);

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace(MODID).register(FissionControllerBE.NAME, SCHEMA);
        event.namespace(MODID).register(TurbineControllerBE.NAME, SCHEMA);
        event.namespace(MODID).register("nc_ore_veins", SCHEMA);
        event.namespace(MODID).register("fusion_core", SCHEMA);
        event.namespace(MODID).register("fusion_coolant", SCHEMA);
        event.namespace(MODID).register("fission_boiling", SCHEMA);
        event.namespace(MODID).special("reset_nbt");
        event.namespace(MODID).special("shielding");
        for (String recipeType : NcRecipeType.ALL_RECIPES.keySet()) {
            event.namespace(MODID).register(recipeType, SCHEMA);
        }
    }

    @Override
    public void onServerReload() {
        NcRecipeType.invalidateCache();
    }
}
