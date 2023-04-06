package igentuman.nc.phosphophyllite;

import igentuman.nc.NuclearCraft;
import igentuman.nc.util.annotation.ClientOnly;
import igentuman.nc.util.annotation.OnModLoad;
import igentuman.nc.util.annotation.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;

public class MultiblockRegistry {
    private interface AnnotationHandler {
        void run(final String modNamespace, final Class<?> clazz, final String memberName);
    }
    public static void init() {
        final Map<String, AnnotationHandler> onModLoadMap = new Object2ObjectOpenHashMap<>();
        String callerClass = new Exception().getStackTrace()[1].getClassName();
        String callerPackage = callerClass.substring(0, callerClass.lastIndexOf("."));

        ModFileScanData modFileScanData = FMLLoader.getLoadingModList().getModFileById(MODID).getFile().getScanResult();

        onModLoadMap.put(OnModLoad.class.getName(), MultiblockRegistry::onModLoadAnnotation);
        handleAnnotationTypes(modFileScanData, callerPackage, MODID, onModLoadMap, true);
    }

    private static void handleAnnotationTypes(ModFileScanData modFileScanData, String callerPackage, String modNamespace, Map<String, AnnotationHandler> annotations, boolean requiredCheck) {
        for (ModFileScanData.AnnotationData annotation : modFileScanData.getAnnotations()) {
            final var annotationClassName = annotation.annotationType().getClassName();
            AnnotationHandler handler = annotations.get(annotationClassName);
            if (handler == null) {
                continue;
            }
            String className = annotation.clazz().getClassName();
            if (className.startsWith(callerPackage)) {
                if (NuclearCraft.LOGGER.isDebugEnabled()) {
                    NuclearCraft.LOGGER.debug("Attempting to handle " + annotationClassName + " in class " + className + " on member " + annotation.memberName());
                }
                try {
                    Class<?> clazz = MultiblockRegistry.class.getClassLoader().loadClass(className);
                    if (clazz.isAnnotationPresent(ClientOnly.class) && !FMLEnvironment.dist.isClient()) {
                        continue;
                    }
                    if (clazz.isAnnotationPresent(SideOnly.class)) {
                        var sideOnly = clazz.getAnnotation(SideOnly.class);
                        if (sideOnly.value() != FMLEnvironment.dist) {
                            continue;
                        }
                    }
                    // class loaded, so, pass it off to the handler
                    handler.run(modNamespace, clazz, annotation.memberName());
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    if (NuclearCraft.LOGGER.isDebugEnabled()) {
                        NuclearCraft.LOGGER.debug("Failed to handle required annotation " + annotation.annotationType().getClassName() + " in class " + className + " on member " + annotation.memberName() + " with error " + e);
                    }
                    if (requiredCheck) {
                        var isRequired = annotation.annotationData().get("required");
                        if (isRequired instanceof Boolean && (Boolean) isRequired) {
                            e.printStackTrace();
                            throw new IllegalStateException("Failed to handle required annotation " + annotation.annotationType().getClassName() + " in class " + className);
                        }
                    }
                }
            }
        }
    }


    private static void onModLoadAnnotation(String modNamespace, Class<?> modLoadClazz, final String memberName) {
        try {
            Method method = modLoadClazz.getDeclaredMethod(memberName.substring(0, memberName.indexOf('(')));

            if (!Modifier.isStatic(method.getModifiers())) {
                NuclearCraft.LOGGER.error("Cannot call non-static @OnModLoad method " + method.getName() + " in " + modLoadClazz.getSimpleName());
                return;
            }

            if (method.getParameterCount() != 0) {
                NuclearCraft.LOGGER.error("Cannot call @OnModLoad method with parameters " + method.getName() + " in " + modLoadClazz.getSimpleName());
                return;
            }

            method.setAccessible(true);
            method.invoke(null);

            if (NuclearCraft.LOGGER.isDebugEnabled()) {
                NuclearCraft.LOGGER.debug("@OnModLoad for " + memberName + " in " + modLoadClazz.getName() + " run");
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            NuclearCraft.LOGGER.warn(modLoadClazz.getName());
            e.printStackTrace();
        }
    }
}
