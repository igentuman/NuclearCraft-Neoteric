package igentuman.nc.client.render.q36;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.client.renderer.BillboardingEffectRenderer;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.client.renderer.ModRenderType;
import igentuman.nc.entity.Q36EnergyFlash;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.NuclearCraft.rl;

public class Q36EnergyFlashRenderer extends EntityRenderer<Q36EnergyFlash, Q36EnergyFlashRenderer.State> {

    private static final Identifier TEXTURE = rl("textures/particle/blackhole_transparent.png");
    private static final int GRID_SIZE = 6;
    private static final int TOTAL_FRAMES = GRID_SIZE * GRID_SIZE;
    private static final float MAX_SCALE = 3.0F;
    private static final long DISTORTION_EXPIRY_MS = 200L;

    public Q36EnergyFlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Q36EnergyFlash entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.pos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

        float ageF = entity.tickCount + partialTick;
        int life = Q36EnergyFlash.LIFETIME_TICKS;
        int growTicks = 6;
        float radiusFactor;
        float magnification;
        if (ageF <= growTicks) {
            float t = ageF / growTicks;
            float ease = 1.0F - (1.0F - t) * (1.0F - t);
            radiusFactor = ease;
            magnification = 1.5F + ease * 14.0F;
        } else {
            float t = Math.min(1.0F, (ageF - growTicks) / (float) (life - growTicks));
            float ease = t * t;
            radiusFactor = 1.0F - ease * 0.5F;
            magnification = Math.max(0.0F, 1.0F - ease * 0.95F);
        }
        if (entity.isRemoved()) {
            return;
        }
        final Vec3 pos = state.pos;
        final float finalRadiusFactor = radiusFactor;
        final float finalMagnification = magnification;
        final long refreshed = Util.getMillis();
        DistortShader.add(entity.getId(), new DistortShader.DistortionSource() {
            @Override public Vec3 position(float pt) { return pos; }
            @Override public float radius()          { return finalRadiusFactor * 300.0F; }
            @Override public float magnification()   { return finalMagnification; }
            @Override public boolean isActive()      { return Util.getMillis() - refreshed <= DISTORTION_EXPIRY_MS; }
        });
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float progress = state.ageInTicks / (float) Q36EnergyFlash.LIFETIME_TICKS;
        progress = Math.min(1.0F, progress);

        float scale;
        if (progress < 0.5F) {
            float t = progress / 0.5F;
            scale = MAX_SCALE * (1.0F - (1.0F - t) * (1.0F - t));
        } else {
            float t = (progress - 0.5F) / 0.5F;
            scale = MAX_SCALE * (1.0F - t * t);
        }
        scale = Math.max(0.01F, scale);

        int alpha = Math.max(0, (int) (255 * (1.0F - progress)));
        int frame = Math.min(TOTAL_FRAMES - 1, (int) (progress * TOTAL_FRAMES));
        int xIndex = frame % GRID_SIZE;
        int yIndex = frame / GRID_SIZE;
        float spriteSize = 1.0F / GRID_SIZE;
        final float minU = xIndex * spriteSize;
        final float maxU = minU + spriteSize;
        final float minV = yIndex * spriteSize;
        final float maxV = minV + spriteSize;

        final Vec3 pos = state.pos;
        final float finalScale = scale;
        final int finalAlpha = alpha;

        DelayedRenderHandler.addTransparentRenderer(ModRenderType.BILLBOARD.apply(TEXTURE),
                new DelayedRenderHandler.LazyRender() {
                    @Override
                    public void render(Camera cam, VertexConsumer vb, PoseStack ps, int renderTick, float pt) {
                        BillboardingEffectRenderer.quad(cam, vb, ps, pos, finalScale, minU, maxU, minV, maxV, 200, 220, 255, finalAlpha);
                    }

                    @Override
                    public Vec3 getCenterPos(float pt) {
                        return pos;
                    }
                });
    }

    @Override
    public boolean shouldRender(Q36EnergyFlash entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        return true;
    }

    public static class State extends EntityRenderState {
        public Vec3 pos = Vec3.ZERO;
    }
}
