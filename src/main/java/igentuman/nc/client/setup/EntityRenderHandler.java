package igentuman.nc.client.setup;

import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.model.ModelFeralGhoulBoss;
import igentuman.nc.client.renderer.FeralGhoulBossRenderer;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL;
import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL_BOSS;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityRenderHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FERAL_GHOUL.get(), FeralGhoulRenderer::new);
        event.registerEntityRenderer(FERAL_GHOUL_BOSS.get(), FeralGhoulBossRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register the model layers
        event.registerLayerDefinition(ModelFeralGhoul.LAYER_LOCATION, ModelFeralGhoul::createBodyLayer);
        event.registerLayerDefinition(ModelFeralGhoulBoss.LAYER_LOCATION, ModelFeralGhoulBoss::createBodyLayer);
    }
}
