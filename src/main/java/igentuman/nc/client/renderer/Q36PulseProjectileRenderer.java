package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.entity.Q36PulseProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class Q36PulseProjectileRenderer extends EntityRenderer<Q36PulseProjectile> {

    public Q36PulseProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(Q36PulseProjectile entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Q36PulseProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
