package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.api.multiblock.part.HeatSinkDef;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipes;
import igentuman.nc.recipe.particle.ParticleRecipes;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.FissionFuel;
import igentuman.nc.setup.entries.FissionReactor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

/** KubeJS plugin registering NuclearCraft's script events and processor recipe schemas. */
public class ModKubeJSPlugin implements KubeJSPlugin {

    private static final RecipeKey<List<SizedIngredient>> ITEM_INPUTS =
            SizedIngredientComponent.FLAT.instance().asList().inputKey("item_inputs").optional(List.of());

    private static final RecipeKey<List<SizedFluidIngredient>> FLUID_INPUTS =
            SizedFluidIngredientComponent.FLAT.instance().asList().inputKey("fluid_inputs").optional(List.of());

    private static final RecipeKey<List<ItemOutput>> ITEM_OUTPUTS =
            NcRecipeComponents.ITEM_OUTPUT.instance().asList().outputKey("item_outputs").optional(List.of());

    private static final RecipeKey<List<FluidOutput>> FLUID_OUTPUTS =
            NcRecipeComponents.FLUID_OUTPUT.instance().asList().outputKey("fluid_outputs").optional(List.of());

    private static final RecipeKey<Integer> PROCESS_TIME =
            NumberComponent.INT.otherKey("process_time");

    private static final RecipeKey<Integer> ENERGY_PER_TICK =
            NumberComponent.INT.otherKey("energy_per_tick");

    private static final RecipeSchema UNIVERSAL_PROCESSOR_SCHEMA = new RecipeSchema(
            ITEM_INPUTS, FLUID_INPUTS, ITEM_OUTPUTS, FLUID_OUTPUTS, PROCESS_TIME, ENERGY_PER_TICK
    );

    private static final RecipeKey<ParticleIngredient> PARTICLE_INPUT =
            NcRecipeComponents.PARTICLE_INGREDIENT.inputKey("particle_input");

    private static final RecipeKey<List<ParticleIngredient>> PARTICLE_INPUTS =
            NcRecipeComponents.PARTICLE_INGREDIENT.instance().asList().inputKey("particle_inputs");

    private static final RecipeKey<List<ParticleStack>> PARTICLE_OUTPUTS =
            NcRecipeComponents.PARTICLE_STACK.instance().asList().outputKey("particle_outputs").optional(List.of());

    private static final RecipeKey<Double> CROSS_SECTION =
            NumberComponent.DOUBLE.otherKey("cross_section");

    private static final RecipeKey<Long> RELEASED_ENERGY =
            NumberComponent.LONG.otherKey("released_energy_kev").optional(0L);

    private static final RecipeKey<Integer> PRIORITY =
            NumberComponent.INT.otherKey("priority").optional(0);

    private static final RecipeSchema TARGET_CHAMBER_SCHEMA = new RecipeSchema(
            PARTICLE_INPUT, CROSS_SECTION, ITEM_OUTPUTS, FLUID_OUTPUTS, PARTICLE_OUTPUTS,
            ITEM_INPUTS, FLUID_INPUTS, RELEASED_ENERGY, PRIORITY
    );

    private static final RecipeSchema DECAY_CHAMBER_SCHEMA = new RecipeSchema(
            PARTICLE_INPUT, PARTICLE_OUTPUTS, CROSS_SECTION, RELEASED_ENERGY, PRIORITY
    );

    private static final RecipeSchema COLLISION_CHAMBER_SCHEMA = new RecipeSchema(
            PARTICLE_INPUTS, PARTICLE_OUTPUTS, CROSS_SECTION, RELEASED_ENERGY,
            BooleanComponent.BOOLEAN.otherKey("ordered_inputs").optional(false), PRIORITY
    );

    private static final RecipeSchema ACCELERATOR_COOLANT_SCHEMA = new RecipeSchema(
            SizedFluidIngredientComponent.FLAT.inputKey("input_fluid"),
            NcRecipeComponents.FLUID_OUTPUT.outputKey("output_fluid"),
            NumberComponent.LONG.otherKey("heat_per_mb"),
            NumberComponent.LONG.otherKey("minimum_temperature_k").defaultOptional()
    );

    private static final RecipeSchema KUGELBLITZ_SCHEMA = new RecipeSchema(
            SizedIngredientComponent.FLAT.inputKey("input"),
            NcRecipeComponents.ITEM_OUTPUT.outputKey("output"),
            NumberComponent.DOUBLE.otherKey("time_modifier").optional(1.0),
            NumberComponent.DOUBLE.otherKey("power_modifier").optional(1.0)
    );

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(NCKubeEvents.GROUP);
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        // Only the startup script pass may add fuels; their items/fluids must be registered before
        // the registries freeze (the startup pass runs during mod setup, ahead of registration).
        if (manager.scriptType != ScriptType.STARTUP) {
            return;
        }
        RegisterFissionFuelKubeEvent fuelEvent = new RegisterFissionFuelKubeEvent();
        NCKubeEvents.REGISTER_FISSION_FUEL.post(ScriptType.STARTUP, fuelEvent);
        for (FuelDef def : fuelEvent.getCollected()) {
            FissionFuel.register(def);
        }

        RegisterHeatSinkKubeEvent heatSinkEvent = new RegisterHeatSinkKubeEvent();
        NCKubeEvents.REGISTER_HEAT_SINK.post(ScriptType.STARTUP, heatSinkEvent);
        for (HeatSinkDef def : heatSinkEvent.getCollected()) {
            FissionReactor.register(def);
        }

        NCKubeEvents.REGISTER_PARTICLE_SOURCE.post(ScriptType.STARTUP, new RegisterParticleSourceKubeEvent());
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        NcRecipeComponents.register(registry);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        ModEntries.init();
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasRecipes()) {
                ResourceLocation recipeTypeId = entry.recipeType().getId();
                registry.register(recipeTypeId, UNIVERSAL_PROCESSOR_SCHEMA);
            }
        }
        registry.register(ParticleRecipes.TARGET_CHAMBER_TYPE.getId(), TARGET_CHAMBER_SCHEMA);
        registry.register(ParticleRecipes.DECAY_CHAMBER_TYPE.getId(), DECAY_CHAMBER_SCHEMA);
        registry.register(ParticleRecipes.COLLISION_CHAMBER_TYPE.getId(), COLLISION_CHAMBER_SCHEMA);
        registry.register(ParticleRecipes.ACCELERATOR_COOLANT_TYPE.getId(), ACCELERATOR_COOLANT_SCHEMA);
        registry.register(KugelblitzRecipes.KUGELBLITZ_TYPE.getId(), KUGELBLITZ_SCHEMA);
    }
}
