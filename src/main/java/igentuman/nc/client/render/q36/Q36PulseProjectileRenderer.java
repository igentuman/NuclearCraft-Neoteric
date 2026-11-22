package igentuman.nc.client.render.q36;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.entity.Q36PulseProjectile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class Q36PulseProjectileRenderer extends EntityRenderer<Q36PulseProjectile, EntityRenderState> {

    public Q36PulseProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    }
}
