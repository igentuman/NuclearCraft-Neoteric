package igentuman.nc.hub;

import igentuman.nc.NuclearCraft;
import igentuman.nc.config.Common;
import net.neoforged.fml.ModList;

public final class HubConfig {

    public static final String CHANNEL = "ncn";

    private HubConfig() {}

    public static boolean isEnabled() {
        return Common.HUB_ENABLED.get();
    }

    public static String baseUrl() {
        return Common.HUB_BASE_URL.get();
    }

    public static String modVersion() {
        return ModList.get().getModContainerById(NuclearCraft.MODID)
                .map(mc -> mc.getModInfo().getVersion().toString())
                .orElse("unknown");
    }
}
