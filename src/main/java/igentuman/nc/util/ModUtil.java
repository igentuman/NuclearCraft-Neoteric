package igentuman.nc.util;

import net.minecraftforge.fml.ModList;

public class ModUtil {
    protected static boolean initialized = false;
    protected static boolean isMekanismLoadeed;
    protected static boolean isMekanismGeneratorsLoadeed;
    protected static boolean isBfrLoaded;
    protected static boolean ccLoaded;
    protected static boolean ieLoaded;
    protected static boolean gtLoaded;
    protected static boolean oc2Loaded;
    protected static boolean kubeJsLoaded;
    protected static boolean isJEILoaded;
    protected static boolean isMMLoaded;
    protected static boolean isEMILoaded;
    protected static void initialize()
    {
        if(initialized)
            return;
        initialized = true;
        isMekanismLoadeed = ModList.get().isLoaded("mekanism");
        isMekanismGeneratorsLoadeed = ModList.get().isLoaded("mekanismgenerators");
        isBfrLoaded = ModList.get().isLoaded("bfr");
        oc2Loaded = ModList.get().isLoaded("oc2r");
        ccLoaded = ModList.get().isLoaded("computercraft");
        ieLoaded = ModList.get().isLoaded("immersiveengineering");
        gtLoaded = ModList.get().isLoaded("gtceu");
        kubeJsLoaded = ModList.get().isLoaded("kubejs");
        isJEILoaded = ModList.get().isLoaded("jei");
        isMMLoaded = ModList.get().isLoaded("mm");
        isEMILoaded = ModList.get().isLoaded("emi");
    }

    public static boolean isEMILoaded() {
        initialize();
        return isEMILoaded;
    }

    public static boolean isMMLoaded() {
        initialize();
        return isMMLoaded;
    }

    public static boolean isJEILoaded() {
        initialize();
        return isJEILoaded;
    }


    public static boolean isKubeJsLoaded() {
        initialize();
        return kubeJsLoaded;
    }

    public static boolean isOC2Loaded() {
        initialize();
        return oc2Loaded;
    }

    public static boolean isMekanismLoaded() {
        initialize();
        return isMekanismLoadeed;
    }

    public static boolean isMekanismGeneratorsLoaded() {
        initialize();
        return isMekanismGeneratorsLoadeed;
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
}
