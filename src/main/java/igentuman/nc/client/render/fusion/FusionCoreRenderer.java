package igentuman.nc.client.render.fusion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FusionCoreRenderer implements BlockEntityRenderer<FusionReactorControllerBE, FusionCoreRenderer.State> {

    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;

    public FusionCoreRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FusionReactorControllerBE be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        BlockState blockState = be.getBlockState();
        blockModelResolver.update(state.centre, blockState, DISPLAY);
        itemModelResolver.updateForTopItem(state.core, new ItemStack(blockState.getBlock().asItem()),
                ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        boolean running = be.hasLevel() && be.isRunning();
        state.fast = running && be.getEfficiency() > 0.5;
        state.idle = !running || be.getEfficiency() < 0.1;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float step = state.fast ? -0.15f : -0.08f;
        float angle = Util.getMillis() * step;
        if (state.idle) {
            angle = 45f;
        }
        angle %= 360;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));
        poseStack.scale(1.4f, 1.25f, 1.4f);
        poseStack.translate(-0.5, 0.135f, -0.5);
        state.centre.submit(poseStack, collector, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5, 1.35, 0.5);
        poseStack.scale(3.8f, 3.8f, 3.8f);
        state.core.submit(poseStack, collector, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    /** Fusion core state: the resolved centre block model, the core item and the spin mode. */
    public static class State extends BlockEntityRenderState {
        public final BlockModelRenderState centre = new BlockModelRenderState();
        public final ItemStackRenderState core = new ItemStackRenderState();
        public boolean fast;
        public boolean idle;
    }
}
