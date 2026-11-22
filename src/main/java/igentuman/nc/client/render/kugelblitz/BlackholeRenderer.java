package igentuman.nc.client.render.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.client.renderer.BillboardingEffectRenderer;
import igentuman.nc.client.renderer.BlackHoleShader;
import igentuman.nc.client.renderer.CustomEffect;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static igentuman.nc.NuclearCraft.rl;

public class BlackholeRenderer implements BlockEntityRenderer<BlackHoleBE, BlackholeRenderer.State> {

    private static final CustomEffect CORE = new CustomEffect(rl("textures/particle/blackhole_glow.png"));

    public BlackholeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BlackHoleBE be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        // Registers the BE with the post-process pass; geometry is skipped while the shader draws it.
        state.shaderActive = BlackHoleShader.submit(be);
        state.formationAge = be.getFormationAge(partialTicks);
        state.blackholeScale = be.getBlackholeScale();
        long gameTime = be.getLevel() == null ? 0L : be.getLevel().getGameTime();
        state.time = ((gameTime % 240000L) + partialTicks) / 20F;
        state.center = Vec3.atCenterOf(be.getBlockPos());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.shaderActive) return;

        BlackHoleGeometryRenderer.submit(state.formationAge, state.blackholeScale, state.time, state.center,
                poseStack, collector, camera);
        float scale = BlackHoleGeometryRenderer.bodyRadius(state.formationAge, state.blackholeScale) * 4F;
        final Vec3 center = state.center;
        BillboardingEffectRenderer.submit(CORE.getTexture(), () -> {
            CORE.setPos(center);
            CORE.setScale(scale);
            return CORE;
        });
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class State extends BlockEntityRenderState {
        public boolean shaderActive;
        public float formationAge;
        public float blackholeScale;
        public float time;
        public Vec3 center = Vec3.ZERO;
    }
}
