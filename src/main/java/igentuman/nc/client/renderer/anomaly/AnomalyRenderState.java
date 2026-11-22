package igentuman.nc.client.renderer.anomaly;

import igentuman.nc.entity.anomaly.AnomalyType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public class AnomalyRenderState extends EntityRenderState {
    public AnomalyType type;
    public Vec3 center = Vec3.ZERO;
    public float scale;
}
