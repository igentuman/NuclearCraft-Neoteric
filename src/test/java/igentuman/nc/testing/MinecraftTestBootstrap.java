package igentuman.nc.testing;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Bootstraps Minecraft registries for JUnit tests that do not launch a modded game. */
public final class MinecraftTestBootstrap {
    private MinecraftTestBootstrap() {}

    public static FMLLoader open() throws ReflectiveOperationException {
        var constructor = FMLLoader.class.getDeclaredConstructor(
                ClassLoader.class, String[].class, Dist.class, boolean.class, Path.class);
        constructor.setAccessible(true);
        var loader = constructor.newInstance(MinecraftTestBootstrap.class.getClassLoader(), new String[0],
                Dist.DEDICATED_SERVER, false, Path.of("."));
        try {
            var loadingList = FMLLoader.class.getDeclaredField("loadingModList");
            loadingList.setAccessible(true);
            loadingList.set(loader, LoadingModList.of(List.of(), List.of(), List.of(), List.of(), List.of(), Map.of()));
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            return loader;
        } catch (ReflectiveOperationException | RuntimeException | Error exception) {
            loader.close();
            throw exception;
        }
    }
}
