package igentuman.nc.handler.event.client;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.bomb.BombFxManager;
import igentuman.nc.client.bomb.DebrisFireEmitter;
import igentuman.nc.client.bomb.GroundSmokeEmitter;
import igentuman.nc.client.bomb.ShockwaveDebrisEmitter;
import igentuman.nc.client.multiblock.MultiblockDebugClient;
import igentuman.nc.client.renderer.ScenePostEffects;
import igentuman.nc.client.storage.ClientContainerInventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public final class ClientWorldCleanup {

    private ClientWorldCleanup() {}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ScenePostEffects.clear();
        Q36BeamRenderer.clear();
        BombFxManager.clear();
        GroundSmokeEmitter.clear();
        DebrisFireEmitter.clear();
        ShockwaveDebrisEmitter.clear();
        MultiblockDebugClient.clear();
        ClientContainerInventory.clear();
    }
}
