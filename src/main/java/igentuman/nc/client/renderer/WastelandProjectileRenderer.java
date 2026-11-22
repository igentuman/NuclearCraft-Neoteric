package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.setup.ModEntries;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class WastelandProjectileRenderer extends EntityRenderer<EntityBlockProjectile, WastelandProjectileRenderer.State> {

    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public WastelandProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockModelResolver = context.getBlockModelResolver();
        shadowRadius = 0.25F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityBlockProjectile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        blockModelResolver.update(state.block,
                ModEntries.get("wasteland_earth").block().get().defaultBlockState(), DISPLAY);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float age = state.ageInTicks;
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 6.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(age * 12.0F));
        poseStack.translate(-0.5D, -0.25D, -0.5D);
        state.block.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public final BlockModelRenderState block = new BlockModelRenderState();
    }
}
