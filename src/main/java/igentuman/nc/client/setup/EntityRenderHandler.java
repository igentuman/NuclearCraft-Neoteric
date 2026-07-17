package igentuman.nc.client.setup;

import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.renderer.WastelandBossRenderer;
import igentuman.nc.client.renderer.anomaly.AnomalyRenderer;
import igentuman.nc.client.renderer.anomaly.GravitationalAnomalyRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL;
import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL_BOSS;
import static igentuman.nc.setup.registration.Entities.GRAVITATIONAL_ANOMALY;
import static igentuman.nc.setup.registration.Entities.ELECTRIC_ANOMALY;
import static igentuman.nc.setup.registration.Entities.RADIOACTIVE_ANOMALY;
import static igentuman.nc.setup.registration.Entities.BURNING_ANOMALY;
import static igentuman.nc.setup.registration.Entities.PSYCHO_ANOMALY;
import static igentuman.nc.setup.registration.Entities.TELEPORTING_ANOMALY;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityRenderHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FERAL_GHOUL.get(), FeralGhoulRenderer::new);
        event.registerEntityRenderer(FERAL_GHOUL_BOSS.get(), WastelandBossRenderer::new);
        event.registerEntityRenderer(GRAVITATIONAL_ANOMALY.get(), GravitationalAnomalyRenderer::new);
        event.registerEntityRenderer(ELECTRIC_ANOMALY.get(), AnomalyRenderer::new);
        event.registerEntityRenderer(RADIOACTIVE_ANOMALY.get(), AnomalyRenderer::new);
        event.registerEntityRenderer(BURNING_ANOMALY.get(), AnomalyRenderer::new);
        event.registerEntityRenderer(PSYCHO_ANOMALY.get(), AnomalyRenderer::new);
        event.registerEntityRenderer(TELEPORTING_ANOMALY.get(), AnomalyRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register the model layers
        event.registerLayerDefinition(ModelFeralGhoul.LAYER_LOCATION, ModelFeralGhoul::createBodyLayer);
        event.registerLayerDefinition(ModelWastelandBoss.LAYER_LOCATION, ModelWastelandBoss::createBodyLayer);
    }
}
