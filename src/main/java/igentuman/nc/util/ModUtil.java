package igentuman.nc.util;

import net.neoforged.fml.ModList;

/** Caches which optional compatibility mods are loaded and exposes them via lazy accessor methods. */
public class ModUtil {
    protected static boolean initialized = false;
    protected static boolean isMekanismLoaded;
    protected static boolean isMekanismGeneratorsLoaded;
    protected static boolean isBfrLoaded;
    protected static boolean ccLoaded;
    protected static boolean ieLoaded;
    protected static boolean gtLoaded;
    protected static boolean oc2Loaded;
    protected static boolean kubeJsLoaded;
    protected static boolean isJEMMLoaded;
    protected static boolean isNCNLoaded;
    protected static boolean isMMLoaded;
    protected static boolean isAe2Loaded;
    protected static boolean isCuriosLoaded;
    protected static boolean isJEILoaded;
    protected static boolean isMbtoolLoaded;

    protected static void initialize()
    {
        if(initialized)
            return;
        initialized = true;
        isMekanismLoaded = ModList.get().isLoaded("mekanism");
        isJEMMLoaded = ModList.get().isLoaded("jei_mekanism_multiblocks");
        isMekanismGeneratorsLoaded = ModList.get().isLoaded("mekanismgenerators");
        isBfrLoaded = ModList.get().isLoaded("bfr");
        oc2Loaded = ModList.get().isLoaded("oc2r");
        ccLoaded = ModList.get().isLoaded("computercraft");
        ieLoaded = ModList.get().isLoaded("immersiveengineering");
        gtLoaded = ModList.get().isLoaded("gtceu");
        kubeJsLoaded = ModList.get().isLoaded("kubejs");
        isNCNLoaded = ModList.get().isLoaded("nuclearcraft");
        isMMLoaded = ModList.get().isLoaded("mm");
        isAe2Loaded = ModList.get().isLoaded("ae2");
        isCuriosLoaded = ModList.get().isLoaded("curios");
        isJEILoaded = ModList.get().isLoaded("jei");
        isMbtoolLoaded = ModList.get().isLoaded("mbtool");
    }
    public static boolean isCuriosLoaded() {
        initialize();
        return isCuriosLoaded;
    }
    public static boolean isMMLoaded() {
        initialize();
        return isMMLoaded;
    }
    public static boolean isKubeJsLoaded() {
        initialize();
        return kubeJsLoaded;
    }

    public static boolean isJEMMLoaded() {
        initialize();
        return isJEMMLoaded;
    }

    public static boolean isOC2Loaded() {
        initialize();
        return oc2Loaded;
    }

    public static boolean isMekanismLoaded() {
        initialize();
        return isMekanismLoaded;
    }

    public static boolean isMekanismGeneratorsLoaded() {
        initialize();
        return isMekanismGeneratorsLoaded;
    }

    public static boolean isBfrLoaded() {
        initialize();
        return isBfrLoaded;
    }

    public static boolean isCcLoaded() {
        initialize();
        return ccLoaded;
    }

    public static boolean isIeLoaded() {
        initialize();
        return ieLoaded;
    }

    public static boolean isGtLoaded() {
        initialize();
        return gtLoaded;
    }

    public static boolean isNCNLoaded() {
        initialize();
        return isNCNLoaded;
    }

    public static boolean isAe2Loaded() {
        initialize();
        return isAe2Loaded;
    }

    public static boolean isJEILoaded() {
        initialize();
        return isJEILoaded;
    }

    public static boolean isMbtoolLoaded() {
        initialize();
        return isMbtoolLoaded;
    }
}
