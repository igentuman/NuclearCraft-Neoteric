package igentuman.nc.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.IntBounds;
import igentuman.nc.api.content.ContentRegistration;
import igentuman.nc.api.content.FuelDef;
import igentuman.nc.api.multiblock.part.HeatSinkDef;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.multiblock.fission.FissionTags;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipes;
import igentuman.nc.recipe.particle.ParticleRecipes;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.RecipeSchemaKind;
import igentuman.nc.setup.ModEntries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/** KubeJS plugin registering NuclearCraft's script events and processor recipe schemas. */
public class ModKubeJSPlugin implements KubeJSPlugin {

    private final List<FissionFuelEntry> scriptFuels = new ArrayList<>();
    private final List<HeatSinkEntry> scriptHeatSinks = new ArrayList<>();

    private static final RecipeKey<List<SizedIngredient>> ITEM_INPUTS =
            NcRecipeComponents.FLAT_SIZED_INGREDIENT.asList().withBounds(IntBounds.OPTIONAL).inputKey("item_inputs").optional(List.of());

    private static final RecipeKey<List<SizedFluidIngredient>> FLUID_INPUTS =
            NcRecipeComponents.FLAT_SIZED_FLUID_INGREDIENT.asList().withBounds(IntBounds.OPTIONAL).inputKey("fluid_inputs").optional(List.of());

    private static final RecipeKey<List<ItemOutput>> ITEM_OUTPUTS =
            NcRecipeComponents.ITEM_OUTPUT.asList().withBounds(IntBounds.OPTIONAL).outputKey("item_outputs").optional(List.of());

    private static final RecipeKey<List<FluidOutput>> FLUID_OUTPUTS =
            NcRecipeComponents.FLUID_OUTPUT.asList().withBounds(IntBounds.OPTIONAL).outputKey("fluid_outputs").optional(List.of());

    private static final RecipeKey<Integer> PROCESS_TIME =
            NumberComponent.INT.otherKey("process_time");

    private static final RecipeKey<Integer> ENERGY_PER_TICK =
            NumberComponent.INT.otherKey("energy_per_tick");

    private static final RecipeSchema UNIVERSAL_PROCESSOR_SCHEMA = new RecipeSchema(
            PROCESS_TIME, ENERGY_PER_TICK, ITEM_INPUTS, FLUID_INPUTS, ITEM_OUTPUTS, FLUID_OUTPUTS
    ).constructor(ITEM_INPUTS, FLUID_INPUTS, ITEM_OUTPUTS, FLUID_OUTPUTS, PROCESS_TIME, ENERGY_PER_TICK);

    private static final RecipeKey<ParticleIngredient> PARTICLE_INPUT =
            NcRecipeComponents.PARTICLE_INGREDIENT.inputKey("particle_input");

    private static final RecipeKey<List<ParticleIngredient>> PARTICLE_INPUTS =
            NcRecipeComponents.PARTICLE_INGREDIENT.asList().inputKey("particle_inputs");

    private static final RecipeKey<List<ParticleStack>> PARTICLE_OUTPUTS =
            NcRecipeComponents.PARTICLE_STACK.asList().withBounds(IntBounds.OPTIONAL).outputKey("particle_outputs").optional(List.of());

    private static final RecipeKey<List<ParticleStack>> REQUIRED_PARTICLE_OUTPUTS =
            NcRecipeComponents.PARTICLE_STACK.asList().withBounds(IntBounds.OPTIONAL).outputKey("particle_outputs");

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
            PARTICLE_INPUT, REQUIRED_PARTICLE_OUTPUTS, CROSS_SECTION, RELEASED_ENERGY, PRIORITY
    );

    private static final RecipeKey<Boolean> ORDERED_INPUTS =
            BooleanComponent.BOOLEAN.otherKey("ordered_inputs").optional(false);

    private static final RecipeSchema COLLISION_CHAMBER_SCHEMA = new RecipeSchema(
            PARTICLE_INPUTS, REQUIRED_PARTICLE_OUTPUTS, CROSS_SECTION, RELEASED_ENERGY, ORDERED_INPUTS, PRIORITY
    );

    private static final RecipeSchema ACCELERATOR_COOLANT_SCHEMA = new RecipeSchema(
            NcRecipeComponents.FLAT_SIZED_FLUID_INGREDIENT.inputKey("input_fluid"),
            NcRecipeComponents.FLUID_OUTPUT.outputKey("output_fluid"),
            NumberComponent.LONG.otherKey("heat_per_mb"),
            NumberComponent.LONG.otherKey("minimum_temperature_k").defaultOptional()
    );

    private static final RecipeSchema KUGELBLITZ_SCHEMA = new RecipeSchema(
            NcRecipeComponents.FLAT_SIZED_INGREDIENT.inputKey("input"),
            NcRecipeComponents.ITEM_OUTPUT.outputKey("output"),
            NumberComponent.DOUBLE.otherKey("time_modifier").optional(1.0),
            NumberComponent.DOUBLE.otherKey("power_modifier").optional(1.0)
    );

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(NCKubeEvents.GROUP);
    }

    @Override
    public void initStartup() {
        // FML constructs NuclearCraft before KubeJS. This one-time hook runs after scripts load
        // and before registry events, so callbacks can modify built-ins and queue new entries.
        RegisterFissionFuelKubeEvent fuelEvent = new RegisterFissionFuelKubeEvent();
        NCKubeEvents.REGISTER_FISSION_FUEL.post(ScriptType.STARTUP, fuelEvent);
        for (FuelDef def : fuelEvent.getCollected()) {
            if (ContentRegistration.registerFissionFuel(def)) {
                scriptFuels.add(ModEntries.FISSION_FUEL.get(def.group + "/" + def.name));
            }
        }

        if (NCKubeEvents.REGISTER_HEAT_SINK.hasListeners()) {
            RegisterHeatSinkKubeEvent heatSinkEvent = new RegisterHeatSinkKubeEvent();
            NCKubeEvents.REGISTER_HEAT_SINK.post(ScriptType.STARTUP, heatSinkEvent);
            for (HeatSinkDef def : heatSinkEvent.getCollected()) {
                if (ContentRegistration.registerHeatSink(def)) {
                    scriptHeatSinks.add(ModEntries.HEAT_SINKS.get(def.name));
                }
            }
        }

        NCKubeEvents.REGISTER_PARTICLE_SOURCE.post(ScriptType.STARTUP, new RegisterParticleSourceKubeEvent());
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        scriptFuels.forEach(fuel -> KubeJSContentAssets.generateAssets(generator, fuel));
        scriptHeatSinks.forEach(sink -> KubeJSContentAssets.generateAssets(generator, sink));
    }

    @Override
    public void generateLang(LangKubeEvent event) {
        scriptFuels.forEach(fuel -> KubeJSContentAssets.generateLang(event, fuel));
        scriptHeatSinks.forEach(sink -> KubeJSContentAssets.generateLang(event, sink));
    }

    @Override
    public void generateData(KubeDataGenerator generator) {
        if (scriptHeatSinks.isEmpty()) {
            return;
        }
        JsonArray sinks = new JsonArray();
        JsonArray passiveSinks = new JsonArray();
        for (HeatSinkEntry sink : scriptHeatSinks) {
            String id = sink.block().getId().toString();
            sinks.add(id);
            if (!sink.def().isActive()) {
                passiveSinks.add(id);
            }
        }
        addBlockTag(generator, FissionTags.REACTOR_INNER.location(), sinks);
        addBlockTag(generator, FissionTags.HEAT_SINKS.location(), passiveSinks);
        addBlockTag(generator, BlockTags.MINEABLE_WITH_PICKAXE.location(), sinks);
        addBlockTag(generator, BlockTags.NEEDS_IRON_TOOL.location(), sinks);
    }

    private static void addBlockTag(KubeDataGenerator generator, Identifier id, JsonArray values) {
        JsonObject tag = new JsonObject();
        tag.add("values", values);
        generator.json(id.withPrefix("tags/block/"), tag);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        NcRecipeComponents.register(registry);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasRecipes() && entry.recipeFamily() != null
                && entry.recipeFamily().schemaKind() == RecipeSchemaKind.UNIVERSAL_PROCESSOR) {
                Identifier recipeTypeId = entry.recipeType().getId();
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
