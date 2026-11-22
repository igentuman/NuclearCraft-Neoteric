package igentuman.nc.client.renderer.anomaly;

import igentuman.nc.entity.anomaly.AnomalyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AnomalyRenderer<T extends AnomalyEntity> extends AbstractAnomalyRenderer<T, AnomalyRenderState> {

    public AnomalyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AnomalyRenderState createRenderState() {
        return new AnomalyRenderState();
    }
}
