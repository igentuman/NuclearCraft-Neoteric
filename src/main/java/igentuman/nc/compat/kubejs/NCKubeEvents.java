package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * KubeJS 7 event group exposing NuclearCraft's startup hooks to scripts.
 *
 * <p>Scripts subscribe via {@code NuclearCraftEvents.registerFissionFuel(event => { ... })}.
 * This replaces the legacy KubeJS 6 {@code EventGroup.startup(...) + StartupEventJS} surface,
 * which no longer exists; the KubeJS 7 equivalent uses {@link dev.latvian.mods.kubejs.event.KubeEvent}.</p>
 */
public class NCKubeEvents {

    public static final EventGroup GROUP = EventGroup.of("NuclearCraftEvents");

    /** Startup event for adding / removing / overriding fission fuels. */
    public static final EventHandler REGISTER_FISSION_FUEL =
            GROUP.startup("registerFissionFuel", () -> RegisterFissionFuelKubeEvent.class);
}
