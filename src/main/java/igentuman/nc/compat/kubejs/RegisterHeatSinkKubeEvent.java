package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.setup.entries.FissionReactor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Script-facing event for managing fission reactor heat sinks.
 *
 * <p>Example KubeJS script:</p>
 * <pre>{@code
 * NuclearCraftEvents.registerHeatSink(event => {
 *     event.register('my_custom', 200, ['water_heat_sink', 'redstone_heat_sink'])
 *     event.remove('glowstone')
 *     event.override('water', def => { def.heat = 100 })
 * })
 * }</pre>
 */
public class RegisterHeatSinkKubeEvent implements KubeEvent {

    private final List<HeatSinkDef> collected = new ArrayList<>();

    /** Registers a new heat sink and returns its def for fluent configuration. */
    public HeatSinkDef register(String name, double heat, String[] rules) {
        HeatSinkDef def = new HeatSinkDef(name).heat(heat).rules(rules);
        collected.add(def);
        return def;
    }

    /** Disables a built-in heat sink (block stays registered but hidden and skipped in validation). */
    public void remove(String name) {
        FissionReactor.remove(name);
    }

    /** Mutates parameters of an existing heat sink in place. */
    public void override(String name, Consumer<HeatSinkDef> mutator) {
        FissionReactor.override(name, mutator);
    }

    public List<HeatSinkDef> getCollected() {
        return collected;
    }
}
