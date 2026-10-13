package igentuman.nc.client.render.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.client.renderer.ModRenderType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BlackHoleGeometryRenderer {

    private static final int SPHERE_LAT = 12;
    private static final int SPHERE_LON = 24;
    private static final int DISK_SEGMENTS = 72;
    private static final int DISK_BANDS = 12;
    private static final int RING_SEGMENTS = 48;
    private static final float DISK_INNER = 1.32F;
    private static final float DISK_OUTER = 3.65F;

    private BlackHoleGeometryRenderer() {
    }

    public static float bodyRadius(BlackHoleBE be, float partialTick) {
        float growth = Math.min(be.getFormationAge(partialTick) / 0.45F, 1F);
        return 0.7F * (0.85F + be.getBlackholeScale() * 0.5F) * (0.25F + 0.75F * growth) * 0.75F;
    }

    public static void render(BlackHoleBE be, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        float age = be.getFormationAge(partialTick);
        float formation = Math.min(age / 2.5F, 1F);
        float wobble = (1F - formation) * (1F - formation);
        float body = bodyRadius(be, partialTick);
        float time = ((mc.level.getGameTime() % 240000L) + partialTick) / 20F;

        Vector3f normal = new Vector3f(0.18F + wobble * 0.25F * Mth.sin(age * 11F), 1F,
                0.25F + wobble * 0.2F * Mth.cos(age * 9F)).normalize();
        Vector3f axis = new Vector3f(1F, -normal.x / normal.y, 0F).normalize();
        Vector3f bitangent = new Vector3f(normal).cross(axis).normalize();
        Vec3 center = Vec3.atCenterOf(be.getBlockPos());
        Vector3f toCamera = camera.getPosition().subtract(center).toVector3f();
        if (toCamera.lengthSquared() < 1.0E-6F) toCamera.set(0F, 0F, 1F);
        toCamera.normalize();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        Matrix4f pose = poseStack.last().pose();
        renderCore(buffer.getBuffer(ModRenderType.BLACK_HOLE_CORE), pose, body, age, wobble);
        VertexConsumer additive = buffer.getBuffer(ModRenderType.BLACK_HOLE_DISK);
        renderDisk(additive, pose, body, axis, bitangent, normal, toCamera, time);
        renderPhotonRing(additive, pose, body, camera.rotation());
        poseStack.popPose();
    }

    private static void renderCore(VertexConsumer consumer, Matrix4f pose, float body, float age, float wobble) {
        for (int i = 0; i < SPHERE_LAT; i++) {
            float theta0 = (float) Math.PI * i / SPHERE_LAT;
            float theta1 = (float) Math.PI * (i + 1) / SPHERE_LAT;
            for (int j = 0; j < SPHERE_LON; j++) {
                float phi0 = (float) (Math.PI * 2D) * j / SPHERE_LON;
                float phi1 = (float) (Math.PI * 2D) * (j + 1) / SPHERE_LON;
                sphereVertex(consumer, pose, body, theta0, phi0, age, wobble);
                sphereVertex(consumer, pose, body, theta1, phi0, age, wobble);
                sphereVertex(consumer, pose, body, theta1, phi1, age, wobble);
                sphereVertex(consumer, pose, body, theta0, phi1, age, wobble);
            }
        }
    }

    private static void sphereVertex(VertexConsumer consumer, Matrix4f pose, float body, float theta, float phi,
                                     float age, float wobble) {
        float horizon = 1F + wobble * (0.10F * Mth.sin(age * 17F + phi * 3F) + 0.055F * Mth.sin(age * 23F - phi * 5F));
        float r = body * horizon;
        float sinTheta = Mth.sin(theta);
        consumer.addVertex(pose, r * sinTheta * Mth.cos(phi), r * Mth.cos(theta), r * sinTheta * Mth.sin(phi))
                .setColor(0, 0, 0, 255);
    }

    private static void renderDisk(VertexConsumer consumer, Matrix4f pose, float body, Vector3f axis,
                                   Vector3f bitangent, Vector3f normal, Vector3f toCamera, float time) {
        float[] radii = new float[DISK_BANDS + 1];
        for (int b = 0; b <= DISK_BANDS; b++) {
            radii[b] = DISK_INNER + (DISK_OUTER - DISK_INNER) * b / DISK_BANDS;
        }
        float step = (float) (Math.PI * 2D) / DISK_SEGMENTS;
        for (int b = 0; b < DISK_BANDS; b++) {
            for (int s = 0; s < DISK_SEGMENTS; s++) {
                float a0 = s * step;
                float a1 = a0 + step;
                diskVertex(consumer, pose, body, axis, bitangent, normal, toCamera, time, radii[b], a0);
                diskVertex(consumer, pose, body, axis, bitangent, normal, toCamera, time, radii[b + 1], a0);
                diskVertex(consumer, pose, body, axis, bitangent, normal, toCamera, time, radii[b + 1], a1);
                diskVertex(consumer, pose, body, axis, bitangent, normal, toCamera, time, radii[b], a1);
            }
        }
    }

    private static void diskVertex(VertexConsumer consumer, Matrix4f pose, float body, Vector3f axis,
                                   Vector3f bitangent, Vector3f normal, Vector3f toCamera, float time,
                                   float r, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        Vector3f p = new Vector3f(axis).mul(r * cos).add(new Vector3f(bitangent).mul(r * sin));

        float envelope = smoothstep(1.32F, 1.65F, r) * (1F - smoothstep(2.5F, 3.65F, r));
        float phase = angle - time * 1.8F / (float) Math.pow(Math.max(r, 1F), 1.5D);
        float filaments = 0.55F + 0.22F * Mth.sin(phase * 7F + r * 18F)
                + 0.14F * Mth.sin(phase * 13F - r * 31F)
                + 0.09F * Mth.sin(phase * 29F + r * 47F);
        float hot = 1F - smoothstep(1.4F, 3.5F, r);
        float hot5 = hot * hot * hot * hot * hot;
        float red = Mth.lerp(hot5, Mth.lerp(hot, 0.32F, 1.0F), 1.0F);
        float green = Mth.lerp(hot5, Mth.lerp(hot, 0.08F, 0.38F), 0.86F);
        float blue = Mth.lerp(hot5, Mth.lerp(hot, 0.28F, 0.075F), 0.58F);
        Vector3f tangent = new Vector3f(normal).cross(p);
        if (tangent.lengthSquared() > 1.0E-6F) tangent.normalize();
        float beaming = Mth.clamp(1F + 0.45F * tangent.dot(toCamera), 0.55F, 1.45F);
        float brightness = 0.4F * (0.75F + filaments * 1.15F) * beaming;
        float density = envelope * (0.5F + 0.4F * filaments);
        float alpha = 1F - (float) Math.exp(-density * 1.4F);

        consumer.addVertex(pose, p.x * body, p.y * body, p.z * body)
                .setColor(Math.min(1F, red * brightness), Math.min(1F, green * brightness),
                        Math.min(1F, blue * brightness), alpha);
    }

    private static void renderPhotonRing(VertexConsumer consumer, Matrix4f pose, float body, Quaternionf facing) {
        float inner = body * 1.02F;
        float peak = body * 1.12F;
        float outer = body * 1.32F;
        float step = (float) (Math.PI * 2D) / RING_SEGMENTS;
        for (int s = 0; s < RING_SEGMENTS; s++) {
            float a0 = s * step;
            float a1 = a0 + step;
            ringVertex(consumer, pose, facing, inner, a0, 0F);
            ringVertex(consumer, pose, facing, peak, a0, 0.6F);
            ringVertex(consumer, pose, facing, peak, a1, 0.6F);
            ringVertex(consumer, pose, facing, inner, a1, 0F);
            ringVertex(consumer, pose, facing, peak, a0, 0.6F);
            ringVertex(consumer, pose, facing, outer, a0, 0F);
            ringVertex(consumer, pose, facing, outer, a1, 0F);
            ringVertex(consumer, pose, facing, peak, a1, 0.6F);
        }
    }

    private static void ringVertex(VertexConsumer consumer, Matrix4f pose, Quaternionf facing, float r,
                                   float angle, float alpha) {
        Vector3f v = new Vector3f(Mth.cos(angle) * r, Mth.sin(angle) * r, 0F);
        facing.transform(v);
        consumer.addVertex(pose, v.x, v.y, v.z).setColor(0.9F, 0.42F, 0.17F, alpha);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0F, 1F);
        return t * t * (3F - 2F * t);
    }
}
