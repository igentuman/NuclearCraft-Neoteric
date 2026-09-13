package igentuman.nc.hub;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraftforge.fml.ModList;

public final class HubConfig {

    public static final String CHANNEL = "ncn";

    private HubConfig() {}

    public static boolean isEnabled() {
        return CommonConfig.DESIGNS_HUB.HUB_ENABLED.get();
    }

    public static String baseUrl() {
        return CommonConfig.DESIGNS_HUB.HUB_BASE_URL.get();
    }

    public static String modVersion() {
        return ModList.get().getModContainerById(NuclearCraft.MODID)
                .map(mc -> mc.getModInfo().getVersion().toString())
                .orElse("unknown");
    }
}
