package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;

import static igentuman.nc.NuclearCraft.rl;

public class WastelandBossRenderer extends MobRenderer<EntityWastelandBoss, ModelWastelandBoss<EntityWastelandBoss>> {
    private static final ResourceLocation TEXTURE = rl("textures/entity/wasteland_boss.png");
    private static final ResourceLocation BOSS_GLOW_TEXTURE = rl("textures/entity/wasteland_boss_glow.png");

    private final RandomSource random = RandomSource.create();

    public WastelandBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelWastelandBoss<>(context.bakeLayer(ModelWastelandBoss.LAYER_LOCATION)), 0.8F);
        this.addLayer(new WastelandBossGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWastelandBoss entity) {
        return TEXTURE;
    }

    @Override
    public void render(EntityWastelandBoss entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        // Scale the entity to make it look more imposing
        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    /**
     * Glow layer for the wasteland boss to make it look more menacing
     */
    private static class WastelandBossGlowLayer extends RenderLayer<EntityWastelandBoss, ModelWastelandBoss<EntityWastelandBoss>> {

        public WastelandBossGlowLayer(WastelandBossRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
                            EntityWastelandBoss entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            if (!entitylivingbaseIn.isInvisible()) {
                ModelWastelandBoss<EntityWastelandBoss> model = this.getParentModel();

                float glowIntensity = 0.5F + 0.5F * (float) Math.sin((entitylivingbaseIn.tickCount + partialTicks) * 0.1F);
                int glowLight = 15728880; // Full bright (15 << 20 | 15 << 4)

                int color = FastColor.ARGB32.color(255, (int)(1.0F * 255), (int)(glowIntensity * 255), (int)(glowIntensity * 255));
                model.renderToBuffer(
                    matrixStackIn,
                    bufferIn.getBuffer(RenderType.eyes(BOSS_GLOW_TEXTURE)), // Use eyes render type for glow effect
                    glowLight,
                    packedLightIn,
                    color
                );
            }
        }
    }
}
