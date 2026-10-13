package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * Renders a {@link CustomEffect} as a camera-facing (billboarded) animated quad at a world position.
 * Generic: works for any texture/position, not bound to a specific block or entity.
 *
 * <p>Call {@link #submit(CustomEffect)} every frame for each effect you want drawn (e.g. from a
 * {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer} or a client tick). The
 * actual draw is deferred to {@link DelayedRenderHandler} so all billboards batch together and
 * depth-sort correctly against translucent geometry.
 */
public final class BillboardingEffectRenderer {

    private BillboardingEffectRenderer() {
    }

    /** Submit an effect for this frame, using its own texture. */
    public static void submit(CustomEffect effect) {
        submit(effect.getTexture(), () -> effect);
    }

    /**
     * Submit an effect for this frame under an explicit texture. The effect is resolved lazily at
     * draw time so callers may share a render type across multiple effects of the same texture.
     */
    public static void submit(ResourceLocation texture, Supplier<CustomEffect> lazyEffect) {
        DelayedRenderHandler.addTransparentRenderer(ModRenderType.BILLBOARD.apply(texture), new DelayedRenderHandler.LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick) {
                BillboardingEffectRenderer.render(camera, buffer, poseStack, renderTick, partialTick, lazyEffect.get());
            }

            @Override
            public Vec3 getCenterPos(float partialTick) {
                return lazyEffect.get().getPos(partialTick);
            }
        });
    }

    private static void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, CustomEffect effect) {
        int gridSize = effect.getTextureGridSize();

        int tick = renderTick % (gridSize * gridSize);
        int yIndex = tick % gridSize, xIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;

        int[] color = effect.getColor();
        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        quad(camera, buffer, poseStack, effect.getPos(partialTick), effect.getScale(),
                minU, maxU, minV, maxV, color[0], color[1], color[2], color[3]);
    }

    /**
     * Emit one camera-facing quad centered on {@code pos}. Writes every vertex element the
     * {@link ModRenderType#BILLBOARD} variants may need (uv, color, overlay, lightmap, normal);
     * elements absent from the active vertex format are ignored by the buffer.
     */
    public static void quad(Camera camera, VertexConsumer buffer, PoseStack poseStack, Vec3 pos, float scale,
                            float minU, float maxU, float minV, float maxV, int r, int g, int b, int a) {
        Quaternionf quaternion = camera.rotation();
        Vector3f[] vertexPos = {new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        for (Vector3f vector3f : vertexPos) {
            quaternion.transform(vector3f);
            vector3f.mul(scale);
            vector3f.add((float) pos.x(), (float) pos.y(), (float) pos.z());
        }
        Vector3f normal = quaternion.transform(new Vector3f(0.0F, 0.0F, 1.0F));

        Matrix4f matrix = poseStack.last().pose();
        vertex(buffer, matrix, vertexPos[0], minU, maxV, r, g, b, a, normal);
        vertex(buffer, matrix, vertexPos[1], maxU, maxV, r, g, b, a, normal);
        vertex(buffer, matrix, vertexPos[2], maxU, minV, r, g, b, a, normal);
        vertex(buffer, matrix, vertexPos[3], minU, minV, r, g, b, a, normal);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Vector3f p, float u, float v,
                               int r, int g, int b, int a, Vector3f normal) {
        buffer.addVertex(matrix, p.x(), p.y(), p.z())
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal.x(), normal.y(), normal.z());
    }
}
