package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface INCConfig {

    String getFileName();

    ForgeConfigSpec getConfigSpec();

    ModConfig.Type getConfigType();

    default boolean addToContainer() {
        return true;
    }
}