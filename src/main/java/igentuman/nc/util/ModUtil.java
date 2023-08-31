package igentuman.nc.util;

import net.minecraftforge.fml.ModList;

public class ModUtil {
    protected static boolean initialized = false;
    protected static boolean isMekanismLoadeed;
    protected static boolean ccLoaded;
    protected static void initialize()
    {
        if(initialized)
            return;
        initialized = true;
        isMekanismLoadeed = ModList.get().isLoaded("mekanism");
        ccLoaded = ModList.get().isLoaded("computercraft");
    }

    public static boolean isMekanismLoadeed() {
        initialize();
        return isMekanismLoadeed;
    }

    public static boolean isCcLoaded() {
        initialize();
        return ccLoaded;
    }
}
