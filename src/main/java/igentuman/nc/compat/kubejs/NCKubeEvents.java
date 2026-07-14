package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/** KubeJS event group exposing NuclearCraft's fission fuel and heat sink startup hooks to scripts. */
public class NCKubeEvents {

    public static final EventGroup GROUP = EventGroup.of("NuclearCraftEvents");

    /** Startup event for adding / removing / overriding fission fuels. */
    public static final EventHandler REGISTER_FISSION_FUEL =
            GROUP.startup("registerFissionFuel", () -> RegisterFissionFuelKubeEvent.class);

    /** Startup event for adding / removing / overriding fission heat sinks. */
    public static final EventHandler REGISTER_HEAT_SINK =
            GROUP.startup("registerHeatSink", () -> RegisterHeatSinkKubeEvent.class);
}
