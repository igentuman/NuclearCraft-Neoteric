package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import igentuman.nc.api.content.ContentRegistration;
import igentuman.nc.api.content.FuelDef;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** KubeJS script event for registering, removing, or overriding fission fuels. */
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
        ContentRegistration.removeFissionFuel(group, name);
    }

    /** Overrides parameters of an existing fuel in place. */
    public void override(String group, String name, Consumer<FuelDef> mutator) {
        ContentRegistration.overrideFissionFuel(group, name, mutator);
    }

    public List<FuelDef> getCollected() {
        return collected;
    }
}
