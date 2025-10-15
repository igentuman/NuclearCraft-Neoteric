package igentuman.nc.handler;

import igentuman.nc.compat.kubejs.NCKubeJsEvents;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.util.ModUtil;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModHandler {

    @SubscribeEvent
    public static void registerReloadListeners(AddReloadListenerEvent event) {
        NcRecipeType.invalidateCache();
    }
}
