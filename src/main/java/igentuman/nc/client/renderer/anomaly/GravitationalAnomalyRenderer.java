package igentuman.nc.client.renderer.anomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import static igentuman.nc.setup.registration.NCBlocks.WASTELAND_EARTH;

/**
 * Gravitational variant renderer: the shared core plus a ring of debris blocks whose count scales with
 * the synced absorbed-block counter, orbiting the centre.
 */
public class GravitationalAnomalyRenderer extends AnomalyRenderer<GravitationalAnomalyEntity> {

    private static final int BLOCKS_PER_ORBITER = 8;
    private static final int MAX_ORBITERS = 36;

    // Orbiting debris belts: radius, plane tilt (deg), spin speed (deg/tick). Different signs => counter-rotation.
    private static final float[] BELT_RADIUS = {1.1F, 1.7F, 2.4F};
    private static final float[] BELT_TILT   = {15.0F, -35.0F, 60.0F};
    private static final float[] BELT_SPEED  = {3.5F, -2.4F, 1.8F};

    public GravitationalAnomalyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected float renderScale(GravitationalAnomalyEntity entity) {
        return (float) entity.massFactor();
    }

    @Override
    public void render(GravitationalAnomalyEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        int total = Math.min(MAX_ORBITERS, entity.getAbsorbedBlocks() / BLOCKS_PER_ORBITER);
        if (total <= 0) {
            return;
        }
        BlockState debris = WASTELAND_EARTH.get().defaultBlockState();
        float age = entity.tickCount + partialTick;
        float mass = (float) entity.massFactor();
        float orbiterScale = 0.45F;
        int belts = BELT_RADIUS.length;

        // Outer belts get more blocks (longer circumference): weight capacity by radius.
        float totalWeight = 0.0F;
        for (int b = 0; b < belts; b++) {
            totalWeight += BELT_RADIUS[b];
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.4D, 0.0D);

        int assigned = 0;
        for (int b = 0; b < belts; b++) {
            int count = (b == belts - 1)
                    ? total - assigned
                    : Math.min(total - assigned, Math.round(total * (BELT_RADIUS[b] / totalWeight)));
            assigned += count;
            if (count <= 0) {
                continue;
            }
            float radius = BELT_RADIUS[b] * mass;
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(BELT_TILT[b]));
            poseStack.mulPose(Vector3f.YP.rotationDegrees((age * BELT_SPEED[b] + b * 37.0F) % 360.0F));
            Random rnd = new Random(210);
            for (int i = 0; i < count; i++) {
                float scale = (float)Math.min(0.7f, Math.max(0.3, orbiterScale + rnd.nextFloat()-0.5f));
                double angle = (Math.PI * 2.0D / count) * i;
                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;
                double oy = Math.sin(angle * 2.0D + age * 0.05D) * 0.15D;
                poseStack.pushPose();
                poseStack.translate(ox, oy, oz);
                poseStack.scale(scale, scale, scale);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(age * 4.0F % 360.0F));
                poseStack.translate(-0.5D, -0.5D, -0.5D);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        debris, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
