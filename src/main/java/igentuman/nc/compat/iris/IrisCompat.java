package igentuman.nc.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

public final class IrisCompat {

    private static Boolean loaded;

    private IrisCompat() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            ModList modList = ModList.get();
            if (modList == null) return false;
            loaded = modList.isLoaded("iris");
        }
        return loaded;
    }

    public static boolean shaderPackInUse() {
        return isLoaded() && Api.shaderPackInUse();
    }

    private static final class Api {

        private static boolean shaderPackInUse() {
            return IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
