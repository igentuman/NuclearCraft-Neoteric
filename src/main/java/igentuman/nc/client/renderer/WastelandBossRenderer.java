package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

import static igentuman.nc.NuclearCraft.rl;

public class WastelandBossRenderer extends MobRenderer<EntityWastelandBoss, WastelandBossRenderState, ModelWastelandBoss> {

    private static final Identifier TEXTURE = rl("textures/entity/wasteland_boss.png");
    private static final Identifier GLOW_TEXTURE = rl("textures/entity/wasteland_boss_glow.png");

    public WastelandBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelWastelandBoss(context.bakeLayer(ModelWastelandBoss.LAYER_LOCATION)), 1.2F);
        addLayer(new GlowLayer(this));
    }

    @Override
    public WastelandBossRenderState createRenderState() {
        return new WastelandBossRenderState();
    }

    @Override
    public void extractRenderState(EntityWastelandBoss entity, WastelandBossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, itemModelResolver);
        state.animationEvent = entity.getAnimationEvent();
        state.animationStartTick = entity.getAnimationStartTick();
    }

    @Override
    public Identifier getTextureLocation(WastelandBossRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(WastelandBossRenderState state, PoseStack poseStack) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }

    private static class GlowLayer extends RenderLayer<WastelandBossRenderState, ModelWastelandBoss> {

        private GlowLayer(WastelandBossRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                           WastelandBossRenderState state, float yRot, float xRot) {
            if (state.isInvisible) {
                return;
            }
            float pulse = 0.65F + Mth.sin(state.ageInTicks * 0.1F) * 0.35F;
            int channel = Mth.floor(pulse * 255.0F);
            int color = ARGB.color(255, channel, channel, channel);
            collector.order(1).submitModel(getParentModel(), state, poseStack, RenderTypes.eyes(GLOW_TEXTURE),
                    LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
        }
    }
}
