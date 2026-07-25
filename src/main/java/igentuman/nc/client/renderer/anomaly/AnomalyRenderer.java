package igentuman.nc.client.renderer.anomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.ModRenderType;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnomalyRenderer<T extends AnomalyEntity> extends EntityRenderer<T> {

    private static final int TICKS_PER_FRAME = 2;
    private static final float SCALE = 2.2F;
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

        DelayedRenderHandler.addTransparentRenderer(ModRenderType.BILLBOARD.apply(texture), new DelayedRenderHandler.LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer vc, PoseStack stack, int renderTick, float pt) {
                int frame = ((int) (age / TICKS_PER_FRAME)) % frames;
                int xIndex = frame % cols;
                int yIndex = frame / cols;
                float ssU = 1.0F / cols;
                float ssV = 1.0F / rows;
                float minU = xIndex * ssU;
                float maxU = minU + ssU;
                float minV = yIndex * ssV;
                float maxV = minV + ssV;

                Quaternionf q = camera.rotation();
                Vector3f[] verts = {
                        new Vector3f(-1.0F, 1.0F, 0.0F),
                        new Vector3f(1.0F, 1.0F, 0.0F),
                        new Vector3f(1.0F, -1.0F, 0.0F),
                        new Vector3f(-1.0F, -1.0F, 0.0F)
                };
                for (Vector3f v : verts) {
                    q.transform(v);
                    v.mul(scale);
                    v.add((float) center.x, (float) center.y, (float) center.z);
                }

                stack.pushPose();
                Matrix4f m = stack.last().pose();
                vc.addVertex(m, verts[0].x(), verts[0].y(), verts[0].z()).setColor(255, 255, 255, 255).setUv(minU, maxV);
                vc.addVertex(m, verts[1].x(), verts[1].y(), verts[1].z()).setColor(255, 255, 255, 255).setUv(maxU, maxV);
                vc.addVertex(m, verts[2].x(), verts[2].y(), verts[2].z()).setColor(255, 255, 255, 255).setUv(maxU, minV);
                vc.addVertex(m, verts[3].x(), verts[3].y(), verts[3].z()).setColor(255, 255, 255, 255).setUv(minU, minV);
                stack.popPose();
            }

            @Override
            public Vec3 getCenterPos(float pt) {
                return center;
            }
        });

        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    protected float renderScale(T entity) {
        return 1.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getAnomalyType().texture();
    }
}
