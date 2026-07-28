package igentuman.nc.handler.event.client;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.bomb.BombFxManager;
import igentuman.nc.client.renderer.AnomalyShader;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.client.storage.ClientContainerInventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientWorldCleanup {

    private ClientWorldCleanup() {}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        DistortShader.clear();
        AnomalyShader.clear();
        Q36BeamRenderer.clear();
        BombFxManager.clear();
        ClientContainerInventory.clear();
    }
}
