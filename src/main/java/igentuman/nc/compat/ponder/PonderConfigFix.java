package igentuman.nc.compat.ponder;

import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.impl.config.CatnipModConfig;
import net.createmod.ponder.impl.client.PonderClientConfig;

/**
 * Registers the Ponder and Catnip CLIENT configs during mod construction, before FML loads
 * CLIENT configs. Upstream registers them in client setup, which is too late on FML 11, so the
 * values stay uninitialised. The duplicate registration from upstream is skipped by
 * {@link igentuman.nc.mixin.NeoForgeCatnipConfigRegistryMixin}.
 */
public final class PonderConfigFix {
    private PonderConfigFix() {
    }

    public static void registerEarly() {
        CatnipConfigRegistry registry = CatnipConfigRegistry.INSTANCE;
        ConfigId catnip = new ConfigId("catnip", ConfigSide.CLIENT);
        if (registry.get(catnip).isEmpty()) {
            CatnipModConfig.register();
        }
        ConfigId ponder = new ConfigId("ponder", ConfigSide.CLIENT);
        if (registry.get(ponder).isEmpty()) {
            registry.register(ponder, PonderClientConfig.INSTANCE);
        }
    }
}
