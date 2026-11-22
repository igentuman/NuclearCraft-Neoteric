package igentuman.nc.client.renderer.anomaly;

import net.minecraft.client.renderer.block.BlockModelRenderState;

public class GravitationalAnomalyRenderState extends AnomalyRenderState {
    public int orbiters;
    public float mass;
    public final BlockModelRenderState debris = new BlockModelRenderState();
}
