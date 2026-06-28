package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.setup.entries.FissionFuel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Script-facing event for managing fission fuels. Newly registered fuels are collected here and
 * registered (items + fluids auto-generated) once the event finishes posting; removals and
 * overrides act on already-registered built-ins immediately.
 *
 * <p>Example KubeJS script:</p>
 * <pre>{@code
 * NuclearCraftEvents.registerFissionFuel(event => {
 *     // add a new fuel; oxide/nitride/zirconium/triso variants are derived automatically
 *     event.register('exotic', 'xyz-300')
 *          .forgeEnergy(2000).heat(500).criticality(40).depletion(120).efficiency(150)
 *          .isotopes(300, 238)
 *
 *     event.remove('uranium', 'leu-235')                  // disable a built-in
 *     event.override('uranium', 'heu-235', def => { def.heat = 400 })  // tweak params
 * })
 * }</pre>
 */
public class RegisterFissionFuelKubeEvent implements KubeEvent {

    private final List<FuelDef> collected = new ArrayList<>();

    /** Begins a new fuel; returns the {@link FuelDef} for fluent parameter configuration. */
    public FuelDef register(String group, String name) {
        FuelDef def = new FuelDef(group, name);
        collected.add(def);
        return def;
    }

    /** Adds a fully-built fuel definition. */
    public void register(FuelDef def) {
        collected.add(def);
    }

    /** Disables a built-in fuel (registry objects cannot be removed, so it is hidden/inert). */
    public void remove(String group, String name) {
        FissionFuel.remove(group, name);
    }

    /** Overrides parameters of an existing fuel in place. */
    public void override(String group, String name, Consumer<FuelDef> mutator) {
        FissionFuel.override(group, name, mutator);
    }

    public List<FuelDef> getCollected() {
        return collected;
    }
}
