package igentuman.nc.handler.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;
import java.util.function.Function;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import static igentuman.nc.NuclearCraft.MODID;

public class NCModConfig extends ModConfig {

    private static final ConfFileTypeHandler NCTOML = new ConfFileTypeHandler();

    private final INCConfig conf;

    public NCModConfig(ModContainer container, INCConfig config) {
        super(config.getConfigType(), config.getConfigSpec(), container, MODID + "/" + config.getFileName() + ".toml");
        this.conf = config;
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return NCTOML;
    }

    private static class ConfFileTypeHandler extends ConfigFileTypeHandler {

        private static Path getPath(Path configBasePath) {
            if (configBasePath.endsWith("serverconfig")) {
                return FMLPaths.CONFIGDIR.get();
            }
            return configBasePath;
        }

        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            return super.reader(getPath(configBasePath));
        }

        @Override
        public void unload(Path configBasePath, ModConfig config) {
            super.unload(getPath(configBasePath), config);
        }
    }
}