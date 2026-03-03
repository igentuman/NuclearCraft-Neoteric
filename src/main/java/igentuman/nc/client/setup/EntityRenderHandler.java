package igentuman.nc.client.setup;

import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.renderer.WastelandBossRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL;
import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL_BOSS;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityRenderHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FERAL_GHOUL.get(), FeralGhoulRenderer::new);
        event.registerEntityRenderer(FERAL_GHOUL_BOSS.get(), WastelandBossRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register the model layers
        event.registerLayerDefinition(ModelFeralGhoul.LAYER_LOCATION, ModelFeralGhoul::createBodyLayer);
        event.registerLayerDefinition(ModelWastelandBoss.LAYER_LOCATION, ModelWastelandBoss::createBodyLayer);
    }
}
