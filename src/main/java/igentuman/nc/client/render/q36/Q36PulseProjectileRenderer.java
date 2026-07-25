package igentuman.nc.client.render.q36;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.entity.Q36PulseProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Q36PulseProjectileRenderer extends EntityRenderer<Q36PulseProjectile> {

    public Q36PulseProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Q36PulseProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(Q36PulseProjectile entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
