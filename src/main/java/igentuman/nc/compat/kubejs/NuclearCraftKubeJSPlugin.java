package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import igentuman.nc.recipes.NcRecipeType;

import static igentuman.nc.NuclearCraft.MODID;

public class NuclearCraftKubeJSPlugin extends KubeJSPlugin {


    RecipeKey<InputFluid[]> INPUT_FLUIDS = FluidComponents.INPUT_ARRAY.key("inputFluids").defaultOptional();
    RecipeKey<InputItem[]> INPUT_ITEMS = ItemComponents.INPUT_ARRAY.key("input").defaultOptional();
    RecipeKey<OutputItem[]> OUTPUT_ITEMS = ItemComponents.OUTPUT_ARRAY.key("output").defaultOptional();
    RecipeKey<OutputFluid[]> OUTPUT_FLUIDS = FluidComponents.OUTPUT_ARRAY.key("outputFluids").defaultOptional();
    RecipeKey<InputParticle[]> INPUT_PARTICLES = ParticleComponents.INPUT_ARRAY.key("inputParticles").defaultOptional();
    RecipeKey<OutputParticle[]> OUTPUT_PARTICLES = ParticleComponents.OUTPUT_ARRAY.key("outputParticles").defaultOptional();
    RecipeKey<Double> POWER_MODIFIER = NumberComponent.DoubleRange.ANY_DOUBLE.min(-1000).max(1000).key("powerModifier").defaultOptional();
    RecipeKey<Double> TIME_MODIFIER = NumberComponent.DoubleRange.ANY_DOUBLE.min(-1000).max(1000).key("timeModifier").defaultOptional();
    RecipeKey<Double> RADIATION_MODIFIER = NumberComponent.DoubleRange.ANY_DOUBLE.min(-1000).max(1000).key("radiation").defaultOptional();
    RecipeKey<Double> CROSS_SECTION = NumberComponent.DoubleRange.ANY_DOUBLE.min(0).max(1000).key("crossSection").defaultOptional();
    RecipeKey<Long> MAX_ENERGY = NumberComponent.LongRange.ANY_LONG.min(0).key("maxEnergy").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(NCRecipeJS.class, NCRecipeJS::new,
            INPUT_ITEMS, INPUT_FLUIDS, INPUT_PARTICLES,
            OUTPUT_ITEMS, OUTPUT_FLUIDS, OUTPUT_PARTICLES,
            POWER_MODIFIER, TIME_MODIFIER, RADIATION_MODIFIER);

    RecipeSchema TARGET_CHAMBER_SCHEMA = new RecipeSchema(NCRecipeJS.class, NCRecipeJS::new,
            INPUT_ITEMS, INPUT_FLUIDS, INPUT_PARTICLES,
            OUTPUT_ITEMS, OUTPUT_FLUIDS, OUTPUT_PARTICLES,
            POWER_MODIFIER, TIME_MODIFIER, RADIATION_MODIFIER, CROSS_SECTION, MAX_ENERGY);
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
         for (String recipeType : NcRecipeType.ALL_RECIPES.keySet()) {
             if(recipeType.equals("target_chamber")) {
                 event.namespace(MODID).register(recipeType, TARGET_CHAMBER_SCHEMA);
             }
             event.namespace(MODID).register(recipeType, SCHEMA);
        }
    }


    @Override
    public void onServerReload() {
        NcRecipeType.invalidateCache();
    }

    @Override
    public void registerEvents() {
        NCKubeJsEvents.GROUP.register();
    }
}
