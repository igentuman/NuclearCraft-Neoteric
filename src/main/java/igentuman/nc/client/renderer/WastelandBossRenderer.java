package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import static igentuman.nc.NuclearCraft.rl;

public class WastelandBossRenderer extends MobRenderer<EntityWastelandBoss, ModelWastelandBoss> {

    private static final ResourceLocation TEXTURE = rl("textures/entity/wasteland_boss.png");
    private static final ResourceLocation GLOW_TEXTURE = rl("textures/entity/wasteland_boss_glow.png");

    public WastelandBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelWastelandBoss(context.bakeLayer(ModelWastelandBoss.LAYER_LOCATION)), 1.2F);
        addLayer(new GlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWastelandBoss entity) {
        return TEXTURE;
    }

    @Override
    public void render(EntityWastelandBoss entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static class GlowLayer extends RenderLayer<EntityWastelandBoss, ModelWastelandBoss> {

        private GlowLayer(WastelandBossRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           EntityWastelandBoss entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isInvisible()) {
                return;
            }
            float pulse = 0.65F + Mth.sin((entity.tickCount + partialTick) * 0.1F) * 0.35F;
            int channel = Mth.floor(pulse * 255.0F);
            int color = FastColor.ARGB32.color(255, channel, channel, channel);
            getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE)),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
        }
    }
}
