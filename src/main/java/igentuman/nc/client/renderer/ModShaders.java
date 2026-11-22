package igentuman.nc.client.renderer;

import igentuman.nc.NuclearCraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class ModShaders {

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        ModRenderType.PIPELINES.forEach(event::registerPipeline);
        event.registerPipeline(DistortShader.PIPELINE);
        event.registerPipeline(AnomalyShader.PIPELINE);
        event.registerPipeline(BlackHoleShader.PIPELINE);
        event.registerPipeline(NukeVolumeRenderer.THERMAL_PIPELINE);
        event.registerPipeline(NukeVolumeRenderer.VOLUME_PIPELINE);
        event.registerPipeline(NukeVolumeRenderer.COMPOSITE_PIPELINE);
    }
}
