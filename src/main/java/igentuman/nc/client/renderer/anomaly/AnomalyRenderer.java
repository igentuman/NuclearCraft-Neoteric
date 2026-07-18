package igentuman.nc.client.renderer.anomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import igentuman.nc.handler.event.client.TickHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import static igentuman.nc.client.renderer.NCRenderType.BLACKHOLE;

/**
 * Shared billboard renderer for all anomaly variants. Draws a camera-facing quad textured from the
 * variant's animated sprite sheet (read row-major in a {@code cols x rows} grid), submitted through the
 * additive/translucent {@code BLACKHOLE} render type via the deferred transparent pass. The variant is
 * read from the entity class, so no synced variant field is required. Ambient particles are emitted
 * from the entity's own client tick.
 */
public class AnomalyRenderer<T extends AnomalyEntity> extends EntityRenderer<T> {

    private static final int TICKS_PER_FRAME = 2;
    private static final float SCALE = 1.6F;
    private static final double Y_OFFSET = 1.2D;

    public AnomalyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        AnomalyType type = entity.getAnomalyType();
        final ResourceLocation texture = type.texture();
        final int cols = type.gridCols();
        final int rows = type.gridRows();
        final int frames = type.frameCount();
        final float age = entity.tickCount + partialTick;
        final float scale = SCALE * renderScale(entity);
        final Vec3 center = entity.position().add(0.0D, Y_OFFSET, 0.0D);

        TickHandler.addTransparentRenderer(BLACKHOLE.apply(texture), new TickHandler.LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer vc, PoseStack stack, int renderTick, float pt, ProfilerFiller profiler) {
                int frame = ((int) (age / TICKS_PER_FRAME)) % frames;
                int xIndex = frame % cols;
                int yIndex = frame / cols;
                float ssU = 1.0F / cols;
                float ssV = 1.0F / rows;
                float minU = xIndex * ssU;
                float maxU = minU + ssU;
                float minV = yIndex * ssV;
                float maxV = minV + ssV;

                Quaternion q = camera.rotation();
                Vector3f[] verts = {
                        new Vector3f(-1.0F, 1.0F, 0.0F),
                        new Vector3f(1.0F, 1.0F, 0.0F),
                        new Vector3f(1.0F, -1.0F, 0.0F),
                        new Vector3f(-1.0F, -1.0F, 0.0F)
                };
                for (Vector3f v : verts) {
                    v.transform(q);
                    v.mul(scale);
                    v.add((float) center.x, (float) center.y, (float) center.z);
                }

                stack.pushPose();
                Matrix4f m = stack.last().pose();
                vc.vertex(m, verts[0].x(), verts[0].y(), verts[0].z()).color(255, 255, 255, 255).uv(minU, maxV).endVertex();
                vc.vertex(m, verts[1].x(), verts[1].y(), verts[1].z()).color(255, 255, 255, 255).uv(maxU, maxV).endVertex();
                vc.vertex(m, verts[2].x(), verts[2].y(), verts[2].z()).color(255, 255, 255, 255).uv(maxU, minV).endVertex();
                vc.vertex(m, verts[3].x(), verts[3].y(), verts[3].z()).color(255, 255, 255, 255).uv(minU, minV).endVertex();
                stack.popPose();
            }

            @Override
            public Vec3 getCenterPos(float pt) {
                return center;
            }

            @Override
            public String getProfilerSection() {
                return "nc.anomaly";
            }
        });

        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    /** Per-variant render-size multiplier. Default 1.0; gravitational overrides to grow with mass. */
    protected float renderScale(T entity) {
        return 1.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getAnomalyType().texture();
    }
}
