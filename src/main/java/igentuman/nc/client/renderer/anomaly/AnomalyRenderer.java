package igentuman.nc.client.renderer.anomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.client.renderer.BillboardingEffectRenderer;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.ModRenderType;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AnomalyRenderer<T extends AnomalyEntity> extends EntityRenderer<T> {

    private static final int TICKS_PER_FRAME = 2;
    private static final float SCALE = 2.2F;
    private static final double Y_OFFSET = 1.2D;
    private static final double MAX_RENDER_DISTANCE = 128.0D;

    public AnomalyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
        Vec3 center = entity.position().add(0.0D, Y_OFFSET, 0.0D);
        if (center.distanceToSqr(camX, camY, camZ) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            return false;
        }
        double radius = renderBoundsRadius(entity);
        return frustum.isVisible(new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius));
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

                BillboardingEffectRenderer.quad(camera, vc, stack, center, scale, minU, maxU, minV, maxV, 255, 255, 255, 255);
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

    protected double renderBoundsRadius(T entity) {
        return SCALE * Math.sqrt(2.0D) * renderScale(entity);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getAnomalyType().texture();
    }
}
