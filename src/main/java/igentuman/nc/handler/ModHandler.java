package igentuman.nc.handler;


import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.util.ModUtil;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class ModHandler {

    @SubscribeEvent
    public static void registerReloadListeners(AddReloadListenerEvent event) {
        NcRecipeType.invalidateCache();
    }
}
