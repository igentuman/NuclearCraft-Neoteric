package igentuman.nc.client.render.q36;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.client.renderer.ModRenderType;
import igentuman.nc.entity.Q36EnergyFlash;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static igentuman.nc.NuclearCraft.rl;

public class Q36EnergyFlashRenderer extends EntityRenderer<Q36EnergyFlash> {

    private static final ResourceLocation TEXTURE = rl("textures/particle/blackhole_transparent.png");
    private static final int GRID_SIZE = 6;
    private static final int TOTAL_FRAMES = GRID_SIZE * GRID_SIZE;
    private static final float MAX_SCALE = 3.0F;

    public Q36EnergyFlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Q36EnergyFlash entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        int tick = entity.tickCount;
        float progress = (tick + partialTick) / (float) Q36EnergyFlash.LIFETIME_TICKS;
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
        float minU = xIndex * spriteSize;
        float maxU = minU + spriteSize;
        float minV = yIndex * spriteSize;
        float maxV = minV + spriteSize;

        Vec3 pos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        final float finalScale = scale;
        final int finalAlpha = alpha;

        int entityId = entity.getId();
        float ageF = tick + partialTick;
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
        final float finalRadiusFactor = radiusFactor;
        final float finalMagnification = magnification;
        DistortShader.add(entityId, new DistortShader.DistortionSource() {
            @Override public Vec3 position(float pt) { return pos; }
            @Override public float radius()          { return finalRadiusFactor * 300.0F; }
            @Override public float magnification()   { return finalMagnification; }
            @Override public boolean isActive()      { return !entity.isRemoved(); }
        });

        DelayedRenderHandler.addTransparentRenderer(ModRenderType.BILLBOARD.apply(TEXTURE),
                new DelayedRenderHandler.LazyRender() {
                    @Override
                    public void render(Camera camera, VertexConsumer vb, PoseStack ps, int renderTick, float pt) {
                        Quaternionf rot = camera.rotation();
                        Vector3f[] verts = {
                            new Vector3f(-1, 1, 0),
                            new Vector3f(1, 1, 0),
                            new Vector3f(1, -1, 0),
                            new Vector3f(-1, -1, 0)
                        };
                        for (Vector3f v : verts) {
                            rot.transform(v);
                            v.mul(finalScale);
                            v.add((float) pos.x, (float) pos.y, (float) pos.z);
                        }
                        ps.pushPose();
                        Matrix4f m = ps.last().pose();
                        vb.addVertex(m, verts[0].x, verts[0].y, verts[0].z).setUv(minU, maxV).setColor(200, 220, 255, finalAlpha);
                        vb.addVertex(m, verts[1].x, verts[1].y, verts[1].z).setUv(maxU, maxV).setColor(200, 220, 255, finalAlpha);
                        vb.addVertex(m, verts[2].x, verts[2].y, verts[2].z).setUv(maxU, minV).setColor(200, 220, 255, finalAlpha);
                        vb.addVertex(m, verts[3].x, verts[3].y, verts[3].z).setUv(minU, minV).setColor(200, 220, 255, finalAlpha);
                        ps.popPose();
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

    @Override
    public ResourceLocation getTextureLocation(Q36EnergyFlash entity) {
        return TEXTURE;
    }
}
