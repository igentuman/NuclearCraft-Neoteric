package igentuman.nc.api.content;

import igentuman.nc.api.catalyst.CatalystDef;
import igentuman.nc.api.catalyst.CatalystRegistry;
import igentuman.nc.api.catalyst.CatalystType;
import igentuman.nc.api.multiblock.part.HeatSinkDef;
import igentuman.nc.api.particle.ParticleSourceData;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.FissionFuel;
import igentuman.nc.setup.entries.FissionReactor;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.function.Consumer;

/**
 * Registers or customizes NuclearCraft content during startup on both physical sides.
 * Java addons call these methods from a {@link NewRegistryEvent} listener on their mod event bus.
 * Declare a required dependency on NuclearCraft with ordering AFTER.
 */
public final class ContentRegistration {

    private ContentRegistration() {}

    /**
     * Adds a catalyst slot to a NuclearCraft processor that already uses catalysts.
     * The processor name is its block ID path, such as {@code manufactory}.
     * The processor GUI holds up to five catalyst slots. Addon slots follow the built-in
     * slots in type ID order. Use the same registrations
     * on the client and server, and keep the slot assignments stable for a saved world.
     *
     * <pre>{@code
     * var type = new CatalystType(Identifier.fromNamespaceAndPath("myaddon", "yield"));
     * CatalystRegistry.register(new CatalystDef(YIELD_UPGRADE, type, host -> new YieldCatalyst(type, host)));
     * ContentRegistration.addProcessorCatalyst("manufactory", type);
     * }</pre>
     *
     * @see CatalystRegistry#register(CatalystDef)
     */
    public static void addProcessorCatalyst(String processor, CatalystType type) {
        ModEntries.addProcessorCatalyst(processor, type);
    }

    /**
     * Queues a new fuel and its item and fluid forms in the nuclearcraft namespace.
     * Returns whether a new group/name was added. Supply the resources for new forms in the addon.
     */
    public static boolean registerFissionFuel(FuelDef definition) {
        if (ModEntries.FISSION_FUEL.containsKey(definition.group + "/" + definition.name)) {
            return false;
        }
        FissionFuel.register(definition);
        return true;
    }

    /** Disables an existing fuel. Its registered items and fluids retain their IDs. */
    public static void removeFissionFuel(String group, String name) {
        FissionFuel.remove(group, name);
    }

    /** Updates the settings of an existing fuel and its variants. */
    public static void overrideFissionFuel(String group, String name, Consumer<FuelDef> update) {
        FissionFuel.override(group, name, update);
    }

    /**
     * Queues a new heat sink block and item, with ID nuclearcraft:&lt;name&gt;_heat_sink.
     * Returns whether a new name was added. Supply assets and add the block to the
     * nuclearcraft:reactor_inner block tag and, for passive sinks, the nuclearcraft:heat_sinks tag in the addon.
     */
    public static boolean registerHeatSink(HeatSinkDef definition) {
        if (ModEntries.HEAT_SINKS.containsKey(definition.name)) {
            return false;
        }
        FissionReactor.register(definition);
        return true;
    }

    /** Disables an existing sink. Its registered block and item retain their IDs. */
    public static void removeHeatSink(String name) {
        FissionReactor.remove(name);
    }

    /** Updates an existing sink's cooling and placement rules. */
    public static void overrideHeatSink(String name, Consumer<HeatSinkDef> update) {
        FissionReactor.override(name, update);
    }

    /** Registers or replaces the charge template for an item type. */
    public static void registerItemParticleSource(Identifier itemId, ParticleSourceData source) {
        ParticleSourceCatalog.registerItemSource(itemId, source);
    }

    /** Registers or replaces a fluid source. The template's capacity is particles per millibucket. */
    public static void registerFluidParticleSource(Identifier fluidId, ParticleSourceData source) {
        ParticleSourceCatalog.registerFluidSource(fluidId, source);
    }

    public static void removeItemParticleSource(Identifier itemId) {
        ParticleSourceCatalog.removeItemSource(itemId);
    }

    public static void removeFluidParticleSource(Identifier fluidId) {
        ParticleSourceCatalog.removeFluidSource(fluidId);
    }
}
