package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.client.model.ModelFeralGhoulBoss;
import igentuman.nc.entity.EntityFeralGhoulBoss;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import static igentuman.nc.NuclearCraft.rl;

public class FeralGhoulBossRenderer extends HumanoidMobRenderer<EntityFeralGhoulBoss, ModelFeralGhoulBoss> {
    private static final ResourceLocation TEXTURE = rl("textures/entity/feral_ghoul_boss.png");

    private static final ResourceLocation BOSS_GLOW_TEXTURE = rl("textures/entity/feral_ghoul_boss_glow.png");

    private final RandomSource random = RandomSource.create();

    public FeralGhoulBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelFeralGhoulBoss(context.bakeLayer(ModelFeralGhoulBoss.LAYER_LOCATION)), 0.8F);

        this.addLayer(new HumanoidArmorLayer<>(this,
                new ModelFeralGhoulBoss(context.bakeLayer(ModelFeralGhoulBoss.LAYER_LOCATION)),
                new ModelFeralGhoulBoss(context.bakeLayer(ModelFeralGhoulBoss.LAYER_LOCATION)),
                context.getModelManager()
        ));

        this.addLayer(new BossGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFeralGhoulBoss entity) {
        return TEXTURE;
    }

    @Override
    public void render(EntityFeralGhoulBoss entity, float entityYaw, float partialTicks, PoseStack poseStack,
                      MultiBufferSource buffer, int packedLight) {
        // Apply regular rendering
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(EntityFeralGhoulBoss entity, PoseStack poseStack, float partialTicks) {
        float scale = 1.5f;
        poseStack.scale(scale, scale, scale);

        super.scale(entity, poseStack, partialTicks);
    }

    @Override
    public void renderNameTag(EntityFeralGhoulBoss entity, net.minecraft.network.chat.Component component,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Show boss name from farther away (doubled distance)
        double nameDistance = this.entityRenderDispatcher.distanceToSqr(entity) * 4.0D;
        if (nameDistance <= 4096.0D) {
            component = net.minecraft.network.chat.Component.literal("⚠ ").withStyle(net.minecraft.ChatFormatting.RED)
                    .append(component);
            super.renderNameTag(entity, component, poseStack, buffer, packedLight);
        }
    }

    /**
     * Glow layer for the boss
     */
    static class BossGlowLayer extends RenderLayer<EntityFeralGhoulBoss, ModelFeralGhoulBoss> {
        public BossGlowLayer(FeralGhoulBossRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight,
                          EntityFeralGhoulBoss entity, float limbSwing, float limbSwingAmount,
                          float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            if (!entity.isDeadOrDying()) {
                float pulseIntensity = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.1F);

                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.eyes(BOSS_GLOW_TEXTURE));
                int overlayCoords = this.getOverlayCoords(entity, 0.0F);
                this.getParentModel().renderToBuffer(matrixStack, vertexConsumer, LightTexture.FULL_BRIGHT,
                        overlayCoords, pulseIntensity, 1f, pulseIntensity, 0.5F);
            }
        }

        private int getOverlayCoords(LivingEntity entity, float partialTicks) {
            return entity.isDeadOrDying() ? 15 << 4 :
                   entity.hurtTime > 0 ? 1 << 4 : 0;
        }
    }
}
